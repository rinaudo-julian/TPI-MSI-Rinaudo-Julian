# Registro de refactorings

Trazabilidad entre cada commit de la etapa de refactoring, la técnica aplicada (catálogo de Fowler), los problemas del [diagnóstico](diagnostico.md) que resuelve y el principio que mejora.

**Regla seguida en todos los pasos:** un refactoring por commit, y los 13 tests de caracterización en verde antes de commitear, sin modificar los archivos `.out`.

## 1. Commits de la etapa (`etapa-diagnostico` → `etapa-refactoring`)

| # | Commit | Refactoring (Fowler) | Resuelve | Principio |
|---|---|---|---|---|
| 1 | `a5c01d8` | Rename Variable / Rename Field / Rename Method | CS-12 | Clean Code: nombres que revelan intención |
| 2 | `904c949` | Extract Class (repositorios) + Encapsulate Collection | CS-09, DT-6 | SRP, ISP (interfaces chicas) |
| 3 | `800c058` | Substitute Algorithm (bucles → consultas al repositorio) | CS-03 (búsquedas duplicadas) | DRY |
| 4 | `2c123ea` | Extract Class (`DatosDePrueba`) | CS-10, DT-5 | SRP |
| 5 | `c893ae2` | Introduce Parameter (inyección por constructor) | CS-16 | DIP |
| 6 | `f263ce4` | Remove Dead Code | CS-14 | Clean Code: sin código muerto |
| 7 | `f364457` | Extract Method (`leerEntero`) | CS-03, CS-17 (catch genérico) | DRY, manejo de errores |
| 8 | `6c63365` | Extract Method (`formatearSolicitud`) | CS-03, DT-3 | DRY, separación comando/consulta |
| 9 | `6704418` | Parameterize Method (`Predicate<Solicitud>`) | CS-02, CS-03 | OCP, DRY |
| 10 | `d1d17cb` | Replace Type Code with Class (enums) | CS-04, CS-06 | Seguridad de tipos |
| 11 | `a62a7b5` | Move Method a los enums | CS-05, CS-11, DT-1, DT-2 | OCP, Tell Don't Ask |
| 12 | `71bc843` | Encapsulate Field + Move Method + Replace Error Code with Exception | CS-07, CS-08, CS-09, CS-17 | Modelo rico, encapsulamiento |
| 13 | `fe4fd92` | Replace Temp with Query / estructura de datos (`EnumMap`) | CS-02, CS-12 (`c1..c4`) | OCP |
| 14 | `bde1179` | Extract Class (`ServicioSolicitudes`) | CS-01, CS-15, DT-4 | SRP |
| 15 | `e6e13fa` | Rename Class + Introduce Parameter (`Scanner`, `PrintStream`) | CS-16 | DIP, testeabilidad |
| 16 | `272dfca` | Move Class (paquetes por capa) | CS-10 | Arquitectura en capas |
| 17 | `76cea78` | Remove Dead Code (métodos de repositorio sin uso) | CS-14 | YAGNI, ISP |
| 18 | `668a9c9` | Extract Method (`leerPrioridad`) | CS-03 | DRY |

## 2. Estado de cada problema del diagnóstico

| ID | Problema | Estado | Dónde |
|---|---|---|---|
| CS-01 | God Class | ✅ Resuelto | Dividido en `MenuConsola`, `ServicioSolicitudes`, repositorios, entidades y `DatosDePrueba` (#2, #4, #14) |
| CS-02 | Long Method | ✅ Resuelto | Método más largo: de 146 a 30 líneas (#7, #8, #9, #13) |
| CS-03 | Duplicated Code | ✅ Resuelto | #3, #7, #8, #9, #18 |
| CS-04 | Primitive Obsession | ✅ Resuelto | `EstadoSolicitud`, `TipoSolicitud`, `Prioridad`, `Area` (#10) |
| CS-05 | Switch Statements | ✅ Resuelto | Las decisiones de negocio viven en los enums (#11). Quedan `switch` de número de menú → enum en `MenuConsola`: son traducción de la interfaz, no reglas de negocio |
| CS-06 | Magic Numbers / Strings | ✅ Resuelto | Enums y `LONGITUD_MAXIMA_DESCRIPCION`. El centinela `-1` se mantiene a propósito (ver EX-1) |
| CS-07 | Data Class | ✅ Resuelto | `Solicitud` encapsulada y con comportamiento (#12) |
| CS-08 | Feature Envy | ✅ Resuelto | `asignarResponsable`, `cambiarEstado`, `rechazar`, `esAltaSinResolver` en `Solicitud` (#12) |
| CS-09 | Encapsulamiento roto | ✅ Resuelto | Campos privados y colecciones de solo lectura (#2, #12) |
| CS-10 | Divergent Change | ✅ Resuelto | Una clase por razón de cambio (#4, #14, #16) |
| CS-11 | Shotgun Surgery | ✅ Resuelto | Un estado o tipo nuevo se declara en su enum. El compilador señala los `switch` sin el caso nuevo |
| CS-12 | Uncommunicative Names | ✅ Resuelto | #1, #13 |
| CS-13 | Comentarios como desodorante | ✅ Resuelto | Los comentarios explicativos se reemplazaron por métodos y tipos con nombre. Quedan solo Javadoc de intención |
| CS-14 | Dead Code | ✅ Resuelto | #6, #17 |
| CS-15 | I/O mezclada con dominio | ✅ Resuelto | Solo `MenuConsola` lee y escribe. El servicio y el dominio no conocen la consola (#14, #15) |
| CS-16 | Acoplamiento temporal / dependencias ocultas | ✅ Resuelto | `Main` como *composition root* (#5, #15) |
| CS-17 | Códigos centinela y `catch (Exception)` | ⚠️ Parcial | `catch` acotado a `NumberFormatException` y errores de negocio como excepción. El `-1` se conserva para no alterar la salida (EX-1) |
| CS-18 | Estilo inconsistente | ✅ Resuelto | Unificado durante las extracciones |
| EX-1 a EX-5 | Defectos externos | ⏸️ Fuera de alcance | Corregirlos cambia el comportamiento observable. Ver DT-11 en [decisiones técnicas](decisiones-tecnicas.md) |

## 3. Métricas antes y después

| Métrica | `estado-inicial` | `estado-refactorizado` |
|---|---|---|
| Tipos (clases, interfaces, enums, records) | 5 | 19 (en 4 paquetes + raíz) |
| Clase más grande | `GestorSolicitudes`: 580 líneas (92 % del sistema) | `MenuConsola`: 274 líneas, solo presentación |
| Método más largo | `buscar()`: 146 líneas | `MenuConsola.buscarSolicitudes()`: 30 líneas |
| Bloque de formateo de solicitud | 5 copias | 1 |
| Lectura de enteros con `try/catch` | 9 copias | 1 |
| Literales de estado en el código | 27 | 0 (solo en el enum) |
| Campos públicos mutables en entidades | 15 | 0 |
| Clases que usan `System.in` / `System.out` | 1 (con toda la lógica) | 0 (solo `Main` los pasa por parámetro) |
| Tests automatizados | 0 | 63 (13 de caracterización + 50 unitarios) |
| Testeable sin consola | No | Sí: dominio, servicio, repositorios y menú |

El total de líneas creció (de 629 a 945) por el encapsulamiento (constructores y getters), las interfaces de los repositorios y los Javadoc. Lo que bajó es lo que importa para mantener el código: el tamaño de cada unidad, la duplicación y el acoplamiento.
