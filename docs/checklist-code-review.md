# Checklist de code review

Revisión técnica del código producido en la etapa de refactoring.

- **Alcance:** diferencias entre `estado-inicial` y `etapa-refactoring` (`git diff estado-inicial etapa-refactoring -- src/main`), más la suite de tests agregada en `etapa-pruebas`.
- **Criterio de cada ítem:** ✅ cumple · ⚠️ cumple con observaciones · ❌ no cumple.
- **Hallazgos:** los ítems con ⚠️ o ❌ se detallan en la sección 2, con severidad y recomendación.

## 1. Checklist

### 1.1 Funcionalidad y comportamiento

| # | Ítem | Resultado | Evidencia |
|---|---|---|---|
| F1 | Los cinco casos de uso siguen funcionando (registrar, cambiar estado, asignar responsable, buscar/filtrar, reporte) | ✅ | 13 tests de caracterización en verde |
| F2 | El comportamiento externo es idéntico al de la versión inicial | ✅ | `.out` sin cambios desde `etapa-diagnostico` ([evidencia](evidencias/02-tests-estado-refactorizado.txt)) |
| F3 | Las reglas de negocio se aplican en todos los caminos | ✅ | Las invariantes viven en `Solicitud` y no se pueden saltear desde afuera |
| F4 | Los casos borde están contemplados (reporte vacío, ID inexistente, descripción de 200/201 caracteres) | ✅ | `ServicioSolicitudesTest`, `SolicitudTest` |
| F5 | El manejo de entradas inválidas es robusto | ⚠️ | H-01, H-02 |

### 1.2 Diseño y principios SOLID

| # | Ítem | Resultado | Evidencia |
|---|---|---|---|
| D1 | **SRP:** cada clase tiene una sola razón de cambio | ✅ | Presentación (`MenuConsola`), casos de uso (`ServicioSolicitudes`), reglas (`Solicitud` y enums), almacenamiento (repositorios), armado (`Main`) |
| D2 | **OCP:** se puede agregar un estado, tipo o filtro sin modificar código existente | ✅ | Enums + `switch` exhaustivo, `Predicate`, `EnumMap` |
| D3 | **LSP:** las implementaciones respetan el contrato de su abstracción | ✅ | Los repositorios `...EnMemoria` cumplen el contrato documentado en cada interfaz |
| D4 | **ISP:** las interfaces son chicas y específicas | ✅ | Se eliminaron los métodos sin uso (`76cea78`) |
| D5 | **DIP:** las capas de alto nivel dependen de abstracciones | ✅ | El servicio depende de interfaces de repositorio y el menú recibe `Scanner`/`PrintStream`. Solo `Main` conoce las implementaciones |
| D6 | Las dependencias entre paquetes apuntan hacia el dominio | ✅ | `dominio` no importa ningún otro paquete del proyecto |
| D7 | Las entidades protegen sus invariantes (sin modelo anémico) | ⚠️ | H-03, H-04 |
| D8 | Los objetos que se exponen son inmutables o de solo lectura | ⚠️ | H-05 |

### 1.3 Clean Code

| # | Ítem | Resultado | Evidencia |
|---|---|---|---|
| C1 | Los nombres revelan intención | ✅ | `a5c01d8` y renames posteriores |
| C2 | Funciones chicas que hacen una sola cosa | ✅ | Método más largo: 30 líneas (antes 146) |
| C3 | Un nivel de abstracción por función | ✅ | Los casos de uso del menú delegan la lógica en el servicio |
| C4 | Sin código duplicado | ✅ | Formateo, lectura de enteros, búsquedas y filtros unificados |
| C5 | Sin valores mágicos | ⚠️ | H-01 (centinela `-1`) |
| C6 | Comentarios solo donde aportan | ✅ | Solo Javadoc de intención o de política de negocio |
| C7 | Errores con excepciones, no con códigos | ✅ | `ReglaDeNegocioException` atrapada en un único lugar |
| C8 | Separación comando/consulta | ✅ | `formatearSolicitud` y `generarReporte` no tienen efectos secundarios |
| C9 | Sin código muerto | ✅ | `f263ce4`, `76cea78` |
| C10 | Formato y estilo consistentes | ✅ | |

### 1.4 Tests

| # | Ítem | Resultado | Evidencia |
|---|---|---|---|
| T1 | Existe una red de seguridad del comportamiento externo | ✅ | `CaracterizacionTest` (13 escenarios) |
| T2 | Las reglas de negocio tienen tests unitarios | ✅ | `EstadoSolicitudTest` (matriz completa de 16 transiciones), `SolicitudTest`, `TipoSolicitudTest` |
| T3 | Los casos de uso se prueban sin infraestructura (sin consola) | ✅ | `ServicioSolicitudesTest` |
| T4 | La presentación se prueba sin estado global | ✅ | `MenuConsolaTest` inyecta entrada y salida |
| T5 | Los tests tienen nombres descriptivos y un solo motivo de fallo | ✅ | |
| T6 | Toda la lógica de los repositorios está cubierta | ⚠️ | H-06 |

### 1.5 Proceso y repositorio

| # | Ítem | Resultado | Evidencia |
|---|---|---|---|
| P1 | Un refactoring por commit, con mensaje descriptivo | ✅ | 18 commits `refactor:` ([registro](registro-refactorings.md)) |
| P2 | Cada commit compila y deja los tests en verde | ✅ | Verificado antes de cada commit |
| P3 | Los tags marcan los hitos | ✅ | `estado-inicial`, `etapa-diagnostico`, `etapa-refactoring`, `etapa-pruebas`, `etapa-docs`, `estado-refactorizado` |
| P4 | No se versionan archivos generados ni del IDE | ✅ | `.gitignore` excluye `target/`, `.idea/`, `*.iml` |

## 2. Hallazgos

| ID | Severidad | Hallazgo | Recomendación |
|---|---|---|---|
| H-01 | Media | `MenuConsola.leerEntero` devuelve `-1` cuando la entrada no es numérica, y ese valor llega al usuario: `Error: no existe la solicitud -1` (EX-1). | Devolver `OptionalInt` y mostrar "el ID debe ser numérico". Cambia la salida: requiere actualizar los `.out` en un commit de corrección, no de refactoring. |
| H-02 | Media | Si la entrada estándar se cierra, `Scanner.nextLine()` lanza `NoSuchElementException` y la aplicación termina con un stack trace (EX-2). | Verificar `hasNextLine()` en el bucle del menú y terminar de forma ordenada. |
| H-03 | Baja | `Solicitud.validarDescripcion` y `rechazar` usan `isEmpty()`, así que dependen de que quien llama haga `trim()`. Desde la consola funciona, pero el servicio aceptaría una descripción de solo espacios. | Usar `isBlank()`. No cambia la salida actual, porque la consola ya recorta la entrada. |
| H-04 | Baja | El constructor de `Solicitud` no valida `null` en `tipo`, `prioridad` ni `legajoEstudiante`. | `Objects.requireNonNull` en el constructor. |
| H-05 | Baja | `ReporteSolicitudes` expone los `EnumMap` mutables que recibe. | Envolverlos con `Collections.unmodifiableMap` en el constructor compacto del `record`. |
| H-06 | Baja | `RepositorioResponsablesEnMemoria` y `RepositorioEstudiantesEnMemoria` no tienen tests propios. Se ejercitan indirectamente desde el servicio y el menú. | Agregar tests análogos a `RepositorioSolicitudesEnMemoriaTest`. |
| H-07 | Info | `asignarId` es público porque los repositorios están en otro paquete. Está protegido contra una segunda asignación, pero cualquier clase podría llamarlo sobre una entidad nueva. | Aceptable en este alcance. Con una base de datos real, el ID lo asignaría la capa de persistencia. |
| H-08 | Info | `DatosDePrueba` vive en el código productivo y se carga siempre al iniciar. | Aceptable para una aplicación de demostración. En un sistema real se activaría por configuración. |

## 3. Veredicto

**Aprobado con observaciones.** El refactoring cumple su objetivo: conserva el comportamiento externo (F1, F2) y resuelve los problemas de calidad interna del diagnóstico (17 de 18 code smells resueltos y uno parcial, ver el [registro](registro-refactorings.md)). Ningún hallazgo bloquea la entrega. H-01 y H-02 son defectos heredados de la versión inicial que se dejaron a propósito fuera del alcance del refactoring (DT-11). H-03 a H-06 son mejoras de bajo costo para una próxima iteración.
