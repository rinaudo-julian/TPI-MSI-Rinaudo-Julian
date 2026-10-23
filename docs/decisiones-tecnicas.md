# Decisiones técnicas

Registro de las decisiones de diseño tomadas durante el trabajo, en formato ADR simplificado: **contexto → decisión → alternativas → consecuencias**.

---

## DT-01 · Java 21 + Maven + JUnit 5

- **Contexto:** el proyecto empezó como un módulo de IntelliJ sin herramienta de build. La etapa de pruebas necesita ejecutar tests de forma reproducible, fuera del IDE.
- **Decisión:** Maven con `maven-surefire-plugin` y JUnit Jupiter 5. Se genera un jar ejecutable con `maven-jar-plugin`.
- **Alternativas:** Gradle (equivalente; Maven es más común en la cátedra) o JUnit agregado a mano como librería (frágil).
- **Consecuencias:** `mvn test` y `mvn package` funcionan en cualquier máquina con JDK 21.

## DT-02 · Tests de caracterización (golden master) antes de refactorizar

- **Contexto:** la versión inicial no tenía tests y su lógica estaba atada a la consola, así que no admitía tests unitarios.
- **Decisión:** 13 escenarios que ejecutan la aplicación completa a través de `Main.main` con una entrada guionada (`.in`) y comparan la salida exacta de consola contra un archivo esperado (`.out`) grabado con la versión inicial. Los `.out` no se modifican durante el refactoring.
- **Alternativas:** escribir tests después del refactoring (no da red de seguridad) o tests unitarios previos (imposibles sin refactorizar antes).
- **Consecuencias:** cualquier cambio en el comportamiento observable rompe un test. Se verificó con una mutación (cambiar el redondeo del porcentaje hizo fallar el escenario 13). Contra: son frágiles ante cambios intencionales de textos, y por eso se complementan con tests unitarios (DT-13).

## DT-03 · Repositorios detrás de interfaces, con implementación en memoria

- **Contexto:** las listas eran campos públicos del gestor y cualquiera podía modificarlas.
- **Decisión:** una interfaz por entidad (`RepositorioSolicitudes`, `RepositorioEstudiantes`, `RepositorioResponsables`) con su implementación `...EnMemoria`. Se inyectan por constructor y `Main` es el único que conoce las implementaciones.
- **Alternativas:** clases concretas sin interfaz (más simple, pero acopla al detalle de almacenamiento) o un `Repositorio<T>` genérico (descartado: las claves son distintas, `int` vs. `String` legajo, y sería una abstracción prematura).
- **Consecuencias:** aplica DIP. Cambiar a persistencia en archivo o base de datos implica una implementación nueva y una línea en `Main`. `buscarTodas()` devuelve una copia inmodificable.

## DT-04 · El repositorio genera los ID; el estudiante usa su legajo

- **Contexto:** el gestor llevaba un contador `ultimoId` que `cargarDatos` y `registrar` incrementaban a mano.
- **Decisión:** `guardar` asigna el siguiente ID cuando la entidad es nueva (`id == 0`). Aplica a `Solicitud` y `Responsable`, que usan **clave sustituta**. `Estudiante` no tiene `id`, porque el legajo es su **clave natural**: lo asigna otro sistema, es único y estable.
- **Regla aplicada:** genera la clave quien es dueño del ciclo de vida de la entidad.
- **Consecuencias:** es imposible desincronizar el contador. `asignarId` es público (el repositorio está en otro paquete), pero rechaza una segunda asignación.

## DT-05 · Uso de `Optional`

- **Contexto:** los repositorios pueden no encontrar un registro.
- **Decisión:** `buscarPorId` y `buscarPorLegajo` devuelven `Optional`. Cuando "no existe" es un error, se usa `orElseThrow(() -> new ReglaDeNegocioException(...))`. Para el nombre del estudiante al formatear se mantiene `.map(Estudiante::getNombre).orElse("")`, que conserva el comportamiento original.
- **Alternativa considerada:** `orElseThrow()` también para el nombre, porque una solicitud con legajo inexistente es un dato inconsistente. Se descartó en el refactoring porque es un cambio de comportamiento, aunque hoy ese caso no puede darse (registrar valida el legajo).
- **Historia:** durante el paso 2 se usó transitoriamente `isEmpty() + get()`. Quedó anotado como deuda y se resolvió en el commit `71bc843`.

## DT-06 · `catch (NumberFormatException)` en lugar de `catch (Exception)`

- **Contexto:** la lectura de enteros atrapaba cualquier excepción y devolvía `-1`.
- **Decisión:** atrapar solo `NumberFormatException`, el único error esperado.
- **Consecuencia (documentada):** si la entrada estándar se corta a mitad de una pregunta, el programa termina en ese momento y no en el menú siguiente. En ambos casos termina con la misma excepción. Ningún escenario de uso normal se ve afectado.

## DT-07 · Enums con comportamiento, y qué regla va en cada lugar

- **Contexto:** estados, tipos, prioridades y áreas eran `String`/`int` y las decisiones sobre ellos estaban repetidas en varios métodos.
- **Decisión:** `EstadoSolicitud`, `TipoSolicitud`, `Prioridad` y `Area` como enums. Criterio para ubicar cada regla:

| Regla | Ubicación | Motivo |
|---|---|---|
| Transiciones permitidas | `EstadoSolicitud.puedePasarA` | Define el ciclo de vida. Depende solo del estado. El `switch` es exhaustivo: un estado nuevo sin transiciones no compila |
| ¿Está cerrado? | `EstadoSolicitud.estaCerrado` | Propiedad intrínseca del estado |
| Área que atiende cada tipo | `TipoSolicitud.getAreaResponsable` | Es una política organizacional, pero fija y simple en este sistema. En uno real vendría de configuración |
| Reclamo de nota ⇒ prioridad ALTA | `ServicioSolicitudes.prioridadSegunPolitica` | Política decidida por secretaría ("puede cambiar"). Describe cómo se registra, no qué es un reclamo |
| Número de menú → enum | `MenuConsola` | Es de la pantalla, no del dominio |

- **Consecuencia:** si el comportamiento por estado creciera mucho, convendría migrar al patrón State.

## DT-08 · Encapsulamiento de `Solicitud` y `ReglaDeNegocioException`

- **Contexto:** modelo anémico, con una clase externa que validaba y mutaba los campos públicos.
- **Decisión:** campos privados, constructor que deja la solicitud `PENDIENTE` y operaciones que protegen sus invariantes: `asignarResponsable`, `cambiarEstado`, `rechazar`. Las violaciones lanzan `ReglaDeNegocioException` con el mensaje que ve el usuario. La consola las atrapa en **un único lugar** y les antepone `Error: `.
- **Detalle de diseño:** `verificarTransicionA` y `verificarQueAdmiteResponsable` existen para que la interfaz pueda validar **antes** de pedir más datos (por ejemplo, el motivo de rechazo se pide solo si la transición es válida). Así se conserva el orden original de preguntas y mensajes.
- **Consecuencia:** desaparecieron los `if → println("Error") → return` repetidos en cada caso de uso.

## DT-09 · `EnumMap` para el reporte, precargado con ceros

- **Decisión:** `contarPor` crea un `EnumMap`, carga todas las constantes con 0 y luego cuenta.
- **Por qué no `Collectors.groupingBy(..., counting())`:** omite las claves sin elementos, así que `RECHAZADA: 0` desaparecería del reporte y cambiaría la salida.
- **Consecuencia:** el orden del reporte es el de declaración del enum, y un valor nuevo aparece en el reporte sin tocar el código que lo imprime.

## DT-10 · Separación en capas: `MenuConsola` y `ServicioSolicitudes`

- **Contexto:** `GestorSolicitudes` leía del `Scanner`, aplicaba reglas e imprimía en el mismo método. Se evaluó mover el menú a `Main` y se descartó: `Main` ya es el *composition root* y es estático, así que no se puede testear ni reutilizar.
- **Decisión:** la línea divisoria es **"preguntar y mostrar" vs. "decidir"**. `MenuConsola` recibe `Scanner` y `PrintStream` por constructor. `ServicioSolicitudes` no conoce la consola: devuelve entidades o un `ReporteSolicitudes` (`record` de solo datos) y lanza excepciones.
- **Orden de trabajo:** primero se limpió el interior de los métodos (pasos L1 a L7) y después se separó. Separar código duplicado obliga a moverlo dos veces.
- **Consecuencia:** el servicio se testea sin consola (`ServicioSolicitudesTest`) y el menú se testea sin tocar `System.in`/`System.out` (`MenuConsolaTest`).

## DT-11 · Defectos externos conservados (EX-1 a EX-4)

- **Decisión:** no se corrigieron durante el refactoring. Son: el `-1` visible para un ID no numérico, la terminación abrupta ante fin de entrada, la mezcla de selección por número y por texto, y "solicitud(es)".
- **Motivo:** un refactoring, por definición, no cambia el comportamiento externo. Corregirlos implica actualizar los `.out` y debe hacerse en un commit aparte, explícito y justificado.

## DT-12 · Eliminación de código sin uso (YAGNI)

- **Decisión:** se eliminaron `GestorSolicitudes.buscarPorId`, los tres `eliminar` de los repositorios y `RepositorioEstudiantes.buscarTodos`, que ningún caso de uso utiliza.
- **Consecuencia:** interfaces más chicas (ISP). Si en el futuro hace falta eliminar, se agrega con su caso de uso y su test.

## DT-13 · Tests unitarios además de los de caracterización

- **Contexto:** los golden master demuestran que el comportamiento externo se conserva, pero no dicen *dónde* falla algo y no cubren todas las combinaciones.
- **Decisión:** 50 tests unitarios: matriz completa de transiciones (16 casos), invariantes de `Solicitud`, casos de uso del servicio con repositorios en memoria, comportamiento del repositorio y del menú con entrada y salida inyectadas.
- **Consecuencia:** son la evidencia de que mejoró la **calidad interna**: nada de esto era testeable en la versión inicial.

## DT-14 · Organización en paquetes por capa

| Paquete | Contenido | Depende de |
|---|---|---|
| `dominio` | Entidades, enums, `ReglaDeNegocioException` | nada |
| `repositorio` | Interfaces y repositorios en memoria | `dominio` |
| `servicio` | Casos de uso y `ReporteSolicitudes` | `dominio`, `repositorio` (interfaces) |
| `consola` | `MenuConsola` | `servicio`, `dominio` |
| raíz | `Main` (*composition root*) y `DatosDePrueba` | todos |

Las dependencias apuntan hacia el dominio, que no depende de ninguna otra capa.
