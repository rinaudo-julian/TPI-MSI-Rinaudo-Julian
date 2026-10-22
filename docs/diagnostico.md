# Diagnóstico técnico — versión inicial

**Versión analizada:** tag `estado-inicial`
**Alcance:** `src/main/java/ar/edu/msi2/solicitudes/` (5 clases, 629 líneas)
**Red de seguridad:** 13 tests de caracterización (`CaracterizacionTest`) que fijan el comportamiento externo observable antes de cualquier refactoring.

Las referencias `Archivo:línea` corresponden al código del tag `estado-inicial`.

---

## 1. Calidad externa vs. calidad interna

### 1.1 Calidad externa (lo que percibe el usuario)

Desde afuera, el sistema **cumple**:

| Caso de uso | Estado | Observaciones |
|---|---|---|
| Registrar solicitud | ✅ Funciona | Valida legajo existente, tipo, descripción (no vacía, ≤ 200 caracteres) y prioridad. Aplica la regla "reclamo de nota ⇒ prioridad ALTA". |
| Cambiar estado | ✅ Funciona | Respeta el ciclo de vida PENDIENTE → EN_PROCESO → RESUELTA, con RECHAZADA desde estados abiertos. Exige responsable para pasar a EN_PROCESO y motivo para rechazar. |
| Asignar responsable | ✅ Funciona | Valida que la solicitud esté abierta y que el área del responsable atienda el tipo de solicitud. Permite reasignar. |
| Buscar / filtrar | ✅ Funciona | Por estado, tipo, legajo y prioridad. Tolera mayúsculas/minúsculas y espacios en estado y tipo. |
| Listar / Reporte | ✅ Funciona | Conteos por estado, tipo y prioridad; porcentaje de resueltas; alertas de prioridad ALTA abierta y de solicitudes sin responsable. |

Defectos externos menores detectados (se documentan, **no** se corrigen durante el refactoring porque cambiarían el comportamiento observable fijado por los tests):

- **EX-1.** Un ID no numérico se informa como `Error: no existe la solicitud -1`: el valor centinela interno se filtra al usuario (consecuencia directa de CS-17).
- **EX-2.** Si la entrada estándar se cierra (EOF), la aplicación termina con `NoSuchElementException` sin mensaje amigable.
- **EX-3.** Inconsistencia de interacción: tipo y estado se eligen por número al registrar o cambiar estado, pero se escriben como texto al filtrar.
- **EX-4.** Mensaje sin pluralización: `Se encontraron 1 solicitud(es).`
- **EX-5.** Los datos se pierden al cerrar la aplicación (aceptable según el enunciado: persistencia simulada en memoria).

### 1.2 Calidad interna (lo que percibe quien mantiene el código)

La calidad interna es **baja**. El sistema "anda", pero es caro de cambiar, de probar y de entender:

| Métrica | Valor | Comentario |
|---|---|---|
| Líneas de `GestorSolicitudes` | 580 de 629 (92 %) | Concentra prácticamente todo el sistema. |
| Método más largo | `buscar()` — 146 líneas (`GestorSolicitudes.java:339-484`) | Contiene cuatro bucles casi idénticos. |
| Bloque de formateo de una solicitud | duplicado **5 veces** | `:373`, `:402`, `:431`, `:465`, `:507` |
| Búsqueda de estudiante por legajo | duplicada **6 veces** | `:111`, `:360`, `:389`, `:418`, `:452`, `:494` |
| `try { Integer.parseInt(...) } catch` | **9 veces** | Un idéntico bloque de lectura de enteros por cada entrada numérica. |
| Literales de estado (`"PENDIENTE"`, etc.) | 27 apariciones | Sin una única fuente de verdad. |
| Clases con comportamiento | 1 | `Solicitud`, `Estudiante` y `Responsable` son contenedores de datos públicos. |
| Testeabilidad | Solo de punta a punta | La lógica está atada a `System.in`/`System.out`; no se puede probar una regla de negocio de forma aislada. |

**Conclusión:** la diferencia entre ambas calidades es el núcleo del problema. El usuario no ve defectos graves, pero cada cambio futuro (un estado nuevo, un tipo nuevo, una interfaz web) es costoso y riesgoso. Esa diferencia es la **deuda técnica**.

---

## 2. Deuda técnica

Clasificación según el cuadrante de Fowler: la mayor parte es deuda **imprudente e inadvertida** ("no sabíamos que esto era un problema"), típica de código escrito rápido para "que funcione".

| ID | Deuda | "Interés" que se paga hoy (costo de cambio) |
|---|---|---|
| DT-1 | Estados representados como `String` sueltos | Agregar un estado (p. ej. `EN_REVISION`) obliga a tocar **6 lugares**: menú de `cambiarEstado` (`:208-212`), `switch` (`:221-236`), validación de transiciones (`:240-251`), chequeo de "cerrada" en `asignar` (`:296`), *prompt* de `buscar` (`:355`) y contadores e impresión de `reporte` (`:520-569`). Olvidar uno no da error de compilación. |
| DT-2 | Tipos de solicitud como `String` y relación tipo → área con `if` | Agregar un tipo toca **4 lugares**: menú y cadena `if` de `registrar` (`:121-145`), mapeo de área en `asignar` (`:324-329`), *prompt* de `buscar` (`:384`) y `reporte` (`:521`, `:531-534`, `:558-562`). |
| DT-3 | Formato de salida de una solicitud duplicado 5 veces | Cambiar cómo se muestra una solicitud (p. ej. agregar fecha) requiere 5 ediciones idénticas, con riesgo de que diverjan. |
| DT-4 | Lógica de negocio mezclada con entrada/salida de consola | Imposible reutilizar las reglas en otra interfaz (web, API, GUI) sin reescribirlas. Imposible probarlas unitariamente. |
| DT-5 | Datos de prueba embebidos en la clase de negocio (`cargarDatos`, `:15-66`) | El sistema no puede arrancar "vacío" ni con otro origen de datos sin modificar la clase central. Obliga a recordar llamar a `cargarDatos()` antes de `menu()` (acoplamiento temporal). |
| DT-6 | Estado interno público (`lista`, `estudiantes`, `responsables`, campos de entidades) | Cualquier clase puede dejar una `Solicitud` en un estado inválido (p. ej. `estado = "CUALQUIERA"`), salteando todas las validaciones. |
| DT-7 | Ausencia de tests automatizados en la versión inicial | Cada cambio exige prueba manual completa. **Mitigada** en esta etapa con los tests de caracterización. |

---

## 3. Catálogo de code smells

Prioridad: **A** = alto impacto en mantenibilidad o bloquea otros refactorings; **M** = medio; **B** = bajo / cosmético.

| ID | Smell (Fowler / Martin) | Ubicación | Evidencia | Refactoring sugerido | Prio |
|---|---|---|---|---|---|
| CS-01 | **Large Class / God Class** | `GestorSolicitudes.java:7-580` | Maneja menú, lectura de consola, validaciones, reglas de negocio, almacenamiento, búsquedas, formateo, reporte y datos de prueba. | Extract Class (UI de consola, servicio de dominio, repositorios, reporte, datos de prueba). | A |
| CS-02 | **Long Method** | `buscar()` `:339-484` (146 líneas), `registrar()` `:107-185` (79), `cambiarEstado()` `:187-275` (89), `asignar()` `:277-337` (61), `reporte()` `:518-570` (53) | Cada método mezcla lectura, validación, regla de negocio y salida. | Extract Method; Decompose Conditional. | A |
| CS-03 | **Duplicated Code** | Formateo de solicitud ×5 (`:357-382`, `:386-411`, `:415-440`, `:449-474`, `:492-513`); búsqueda de estudiante ×6; búsqueda de solicitud por id ×2 (`:196`, `:286`) + `buscarPorId` sin usar (`:572`); lectura de entero ×9; los 4 filtros de `buscar` son el mismo bucle con distinta condición; alta de solicitudes de ejemplo ×4 (`:25-65`). | Mismo código copiado y pegado. | Extract Method (`formatear`, `leerEntero`, `buscarSolicitud`, `buscarEstudiante`); Parameterize Method / filtro con `Predicate<Solicitud>`. | A |
| CS-04 | **Primitive Obsession** | `Solicitud.estado`, `Solicitud.tipo` (`String`), `Solicitud.prioridad` (`int`), `Responsable.area` (`String`) | El comentario `// 1 = alta, 2 = media, 3 = baja` (`Solicitud.java:8`) documenta lo que el tipo debería expresar. | Replace Type Code with Class / `enum` (`EstadoSolicitud`, `TipoSolicitud`, `Prioridad`, `Area`). | A |
| CS-05 | **Switch Statements / cadenas condicionales por tipo** | `registrar` `:134-145`, `cambiarEstado` `:221-236` y `:240-251`, `asignar` `:324-329`, etiqueta de prioridad ×5, `reporte` `:526-538` | La misma decisión ("según el estado/tipo/prioridad…") se repite en varios métodos. | Replace Conditional with Polymorphism / mover la decisión al `enum` (p. ej. `TipoSolicitud.getArea()`, `EstadoSolicitud.puedePasarA(...)`). | A |
| CS-06 | **Magic Numbers / Magic Strings** | `"PENDIENTE"`… (27 veces), `"ALUMNADO"`, `"DEPARTAMENTO ACADEMICO"`, `1/2/3` como prioridad, `200` (`:153`), `-1` como centinela | Valores con significado de negocio sin nombre. | Replace Magic Literal with Symbolic Constant / `enum`. | M |
| CS-07 | **Data Class** (modelo anémico) | `Solicitud.java`, `Estudiante.java`, `Responsable.java` | Solo campos públicos, sin comportamiento ni invariantes. | Encapsulate Field; Move Method (llevar comportamiento a la entidad). | A |
| CS-08 | **Feature Envy** | `cambiarEstado` `:239-274`, `asignar` `:296-335`, `reporte` `:540` | `GestorSolicitudes` decide sobre transiciones, cierre y asignación usando solo datos de `Solicitud`. | Move Method a `Solicitud` (`cambiarEstado`, `asignarResponsable`, `estaCerrada`, `esAltaSinResolver`). | A |
| CS-09 | **Inappropriate Intimacy / encapsulamiento roto** | `GestorSolicitudes.java:9-11`, campos públicos de las entidades | Colecciones internas y estado de las entidades expuestos y mutables desde afuera. | Encapsulate Collection; Encapsulate Field. | M |
| CS-10 | **Divergent Change** | `GestorSolicitudes` | Cambia por motivos no relacionados: textos del menú, reglas de transición, formato del reporte, datos de prueba. | Extract Class (ver CS-01). | A |
| CS-11 | **Shotgun Surgery** | Ver DT-1 y DT-2 | Un único cambio conceptual (nuevo estado o tipo) obliga a modificar muchos puntos. | Centralizar en `enum` (CS-04/CS-05). | A |
| CS-12 | **Uncommunicative Names** | `lista`, `sc`, `s`, `x`, `aux`, `d`, `p`, `t`, `op`, `leg`, `nom`, `pr`, `ok`, `ant`, `porc`, `c1..c4`, `t1..t4`, `p1..p3`, `ultId`, `desc`, `obs`, `resp`; métodos `asignar()`, `buscar()` | Los nombres no revelan intención (`c3` = cantidad de resueltas). | Rename Variable / Rename Method / Rename Field. | M |
| CS-13 | **Comments (como desodorante)** | `// validar transicion` (`:239`), `// cada area atiende ciertos tipos` (`:323`), comentarios de campos en `Solicitud.java:5-11`, `// Clase principal del sistema` (`:6`) | Los comentarios explican bloques que deberían ser métodos o tipos con nombre propio. | Extract Method con nombre explicativo; Replace Type Code. | B |
| CS-14 | **Dead Code** | `buscarPorId` sin uso (`:572-579`); línea comentada (`:514`); rama `else if … ok = false;` sin efecto (`:249-251`) | Código que no se ejecuta o no cambia nada. | Remove Dead Code (o reutilizar `buscarPorId` al extraer la búsqueda). | B |
| CS-15 | **Mezcla de niveles de abstracción / I/O acoplada al dominio** | Todos los métodos públicos de `GestorSolicitudes` | Un mismo método lee `Scanner`, aplica reglas de negocio, muta datos e imprime con `System.out`. | Separar capa de presentación (consola) de la de servicio; el servicio devuelve resultados o lanza excepciones de dominio. | A |
| CS-16 | **Acoplamiento temporal / dependencias ocultas** | `Main.java:5-7`, `GestorSolicitudes.java:13` | `cargarDatos()` debe invocarse antes de `menu()`; el `Scanner` se crea internamente sobre `System.in`. | Inyección de dependencias por constructor; clase de datos de prueba; `Main` como *composition root*. | M |
| CS-17 | **Manejo de errores con códigos centinela y `catch (Exception)` genérico** | 9 bloques `try/catch` que devuelven `-1` | Se traga cualquier excepción y se usa `-1` como "valor inválido", lo que termina mostrado al usuario (EX-1). | Extraer `leerEntero` único; excepciones de dominio con mensaje (manteniendo los mismos textos). | M |
| CS-18 | **Inconsistencia de estilo** | Bucles con índice vs. *for-each*; búsquedas con y sin `break` (`:111` vs `:196`); `if` de una línea sin llaves en `reporte` (`:526-538`) vs. llaves en el resto | El mismo problema se resuelve de maneras distintas. | Unificar al extraer métodos (Clean Code: formato consistente). | B |

---

## 4. Principios SOLID

| Principio | ¿Se cumple? | Análisis |
|---|---|---|
| **S — Responsabilidad única** | ❌ No | `GestorSolicitudes` tiene al menos **siete** razones de cambio: interacción por consola, validación de datos de entrada, reglas de ciclo de vida, reglas de asignación por área, almacenamiento en memoria, formateo/reporte y datos de prueba. |
| **O — Abierto/cerrado** | ❌ No | No se puede agregar un estado, tipo o prioridad sin modificar código existente en varios métodos (DT-1, DT-2). Las reglas de transición codificadas con `if` (`:240-251`) son el ejemplo más claro: una tabla de transiciones en el `enum` de estados permitiría extender sin modificar. |
| **L — Sustitución de Liskov** | — No aplica | La versión inicial no usa herencia ni interfaces. Debe vigilarse si el refactoring introduce polimorfismo (p. ej. estados como clases o repositorios intercambiables): cualquier implementación debe respetar el contrato de la abstracción. |
| **I — Segregación de interfaces** | — No aplica | No hay interfaces. Al introducir abstracciones (p. ej. repositorios), conviene mantenerlas pequeñas y específicas para cada cliente. |
| **D — Inversión de dependencias** | ❌ No | La lógica de alto nivel depende directamente de detalles concretos: `new Scanner(System.in)` (`:13`), `System.out` en todos los métodos y `ArrayList` públicas como almacenamiento. Nada se inyecta; nada puede sustituirse en un test. |

---

## 5. Clean Code

| Regla | Situación | Referencias |
|---|---|---|
| Nombres que revelan intención | ❌ | CS-12 |
| Funciones pequeñas que hacen una sola cosa | ❌ | CS-02, CS-15 |
| Un nivel de abstracción por función | ❌ | Entrada, regla de negocio y salida en el mismo método (CS-15). |
| DRY (no repetirse) | ❌ | CS-03 |
| Sin valores mágicos | ❌ | CS-06 |
| Comentarios solo cuando el código no puede expresarse | ❌ | CS-13 |
| Manejo de errores claro (excepciones en vez de códigos) | ❌ | CS-17 |
| Separación comando/consulta | ❌ | Los métodos leen, mutan e imprimen a la vez; `buscar` filtra e imprime sin devolver resultados. |
| Formato consistente | ⚠️ Parcial | CS-18 |
| Sin código muerto | ❌ | CS-14 |
| Encapsulamiento | ❌ | CS-07, CS-09 |

---

## 6. Plan de refactoring sugerido

Orden propuesto: de lo más seguro y local a lo más estructural. Cada paso se commitea por separado y se verifica con `mvn test` (los 13 tests de caracterización deben permanecer en verde **sin modificar los archivos `.out`**).

| Paso | Refactoring (catálogo de Fowler) | Resuelve |
|---|---|---|
| 1 | **Remove Dead Code**: eliminar la línea comentada y la rama sin efecto; reutilizar o eliminar `buscarPorId`. | CS-14 |
| 2 | **Extract Method** para la lectura de enteros (`leerEntero(String prompt)`), búsqueda de solicitud por id, búsqueda de estudiante por legajo y formateo de una solicitud. | CS-03, CS-17 (parcial), CS-02 (parcial) |
| 3 | **Rename** variables, campos y métodos con nombres que revelen intención. | CS-12 |
| 4 | **Replace Type Code with Class**: `enum EstadoSolicitud`, `enum TipoSolicitud` (con su área responsable y su opción de menú), `enum Prioridad` (con su etiqueta). | CS-04, CS-06 |
| 5 | **Replace Conditional with Polymorphism** / mover decisiones a los `enum`: transiciones permitidas en `EstadoSolicitud`, área en `TipoSolicitud`, etiqueta en `Prioridad`. | CS-05, CS-11, OCP |
| 6 | **Encapsulate Field** + **Move Method**: `Solicitud` con campos privados y comportamiento (`cambiarEstado`, `asignarResponsable`, `estaCerrada`, `esAltaSinResolver`); excepciones de dominio con los mismos mensajes. | CS-07, CS-08, CS-09 |
| 7 | **Parameterize Method**: unificar los cuatro filtros de `buscar` en un único método que recibe un criterio (`Predicate<Solicitud>`). | CS-03, CS-02 |
| 8 | **Extract Class**: `RepositorioSolicitudes` / `RepositorioEstudiantes` / `RepositorioResponsables` (en memoria), `ServicioSolicitudes` (casos de uso), `ReporteSolicitudes` (cálculo de conteos con `EnumMap`), `MenuConsola` (I/O) y `DatosDePrueba`. | CS-01, CS-10, CS-15, SRP |
| 9 | **Introduce Parameter / inyección por constructor**: `MenuConsola` recibe `Scanner` y `PrintStream`; el servicio recibe los repositorios; `Main` arma el grafo de objetos. | CS-16, DIP |

### Restricciones para conservar el comportamiento externo

Para que los tests de caracterización sigan en verde, el refactoring debe preservar exactamente:

- Todos los textos de la consola, incluidos los mensajes de error, `Se encontraron N solicitud(es).` y `Error: no existe la solicitud -1` (EX-1).
- El orden de los conteos del reporte: estados `PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA`; tipos `CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA`; prioridades `ALTA, MEDIA, BAJA`. Si se usan `enum`, el orden de declaración debe coincidir.
- El orden de las validaciones en cada caso de uso: por ejemplo, en `cambiarEstado` se valida la transición **antes** que el responsable, y el motivo de rechazo se pide **después** de ambas.
- El redondeo hacia abajo del porcentaje de resueltas (división entera).
- La regla de negocio "reclamo de nota ⇒ prioridad ALTA", que se aplica **después** de validar la prioridad ingresada.
- La normalización de la entrada en los filtros por estado y tipo (`trim`, mayúsculas, espacios → `_`).

Los defectos externos EX-1 a EX-4 quedan fuera del alcance del refactoring. Corregirlos es un **cambio de comportamiento** que, de hacerse, debe ir en un commit separado y actualizar los `.out` de forma explícita y justificada.
