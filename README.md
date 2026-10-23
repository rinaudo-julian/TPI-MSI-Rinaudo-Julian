# TPI Metodología de Sistemas II — Rinaudo Julián

**Sistema de Gestión de Solicitudes Académicas**: aplicación de consola en Java que permite registrar solicitudes de estudiantes, clasificarlas por tipo, prioridad y estado, asignarles un responsable, buscarlas y obtener un reporte.

El trabajo parte de una versión inicial que funciona pero tiene problemas de diseño reales. Esa versión se diagnostica, se refactoriza en pasos chicos y seguros, y se demuestra con tests que el comportamiento externo se conserva mientras mejora la calidad interna.

## Funcionalidades

| Caso de uso | Descripción |
|---|---|
| Registrar solicitud | Alta para un estudiante existente, con tipo, descripción (hasta 200 caracteres) y prioridad. Los reclamos de nota se registran siempre con prioridad ALTA. |
| Cambiar estado | Ciclo de vida `PENDIENTE → EN_PROCESO → RESUELTA`, con `RECHAZADA` desde cualquier estado abierto. Para pasar a `EN_PROCESO` hace falta un responsable, y para rechazar, un motivo. |
| Asignar responsable | Solo a solicitudes abiertas y solo responsables del área que atiende ese tipo: Alumnado (certificados, cambios de comisión) o Departamento Académico (equivalencias, reclamos de nota). |
| Buscar / filtrar | Por estado, tipo, legajo del estudiante o prioridad. |
| Listar | Todas las solicitudes con su estudiante, prioridad, estado, responsable y motivo de rechazo. |
| Reporte | Totales por estado, tipo y prioridad, porcentaje de resueltas, solicitudes de prioridad ALTA sin resolver y solicitudes sin responsable. |

Entidades principales: `Solicitud`, `Estudiante`, `Responsable` (con su `Area`) y los enums `EstadoSolicitud`, `TipoSolicitud` y `Prioridad`. Los datos se guardan en memoria y la aplicación arranca con datos de ejemplo.

## Tecnologías

- Java 21
- Maven 3.9+
- JUnit 5 (Jupiter, con tests parametrizados)

## Instalación y uso

```bash
git clone https://github.com/rinaudo-julian/TPI-MSI-Rinaudo-Julian.git
cd TPI-MSI-Rinaudo-Julian

mvn package                               # compila, corre los tests y genera el jar
java -jar target/gestion-solicitudes.jar  # ejecuta la aplicación
```

Para correr solo los tests:

```bash
mvn test
```

Datos de ejemplo disponibles al iniciar:

| Estudiantes (legajo) | Responsables (id) |
|---|---|
| 1001 Ana Gomez · 1002 Bruno Diaz · 1003 Carla Perez | 1 Laura Fernandez (Alumnado) · 2 Marcos Ruiz (Alumnado) · 3 Silvia Torres (Depto. Académico) |

## Arquitectura (versión refactorizada)

```
ar.edu.msi2.solicitudes
├── Main                 composition root: arma los objetos y arranca el menú
├── DatosDePrueba        carga los datos de ejemplo
├── consola/             MenuConsola: lee, delega y muestra (única clase que usa la consola)
├── servicio/            ServicioSolicitudes (casos de uso) y ReporteSolicitudes
├── dominio/             entidades, enums con reglas de negocio y ReglaDeNegocioException
└── repositorio/         interfaces de repositorio e implementaciones en memoria
```

Las dependencias apuntan hacia el dominio. El servicio depende de las *interfaces* de los repositorios, y solo `Main` conoce las implementaciones (DIP).

## Tests

| Suite | Tests | Qué verifica |
|---|---|---|
| `CaracterizacionTest` | 13 | *Golden master*: ejecuta la aplicación completa con entradas guionadas y compara la salida exacta contra la grabada con la versión inicial. Demuestra que el comportamiento externo se conservó. |
| `dominio/*Test` | 34 | Matriz completa de transiciones de estado, áreas por tipo e invariantes de `Solicitud`. |
| `servicio/ServicioSolicitudesTest` | 9 | Casos de uso con repositorios en memoria, sin consola. |
| `repositorio/RepositorioSolicitudesEnMemoriaTest` | 4 | Generación de ID, reemplazo y listas de solo lectura. |
| `consola/MenuConsolaTest` | 3 | Menú con entrada y salida inyectadas, sin `System.in`/`System.out`. |

## Etapas del trabajo (tags)

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base con problemas de diseño |
| `etapa-diagnostico` | Tests de caracterización y diagnóstico técnico |
| `etapa-refactoring` | Último commit de los 18 refactorings |
| `etapa-pruebas` | Tests unitarios del diseño nuevo y evidencia |
| `etapa-docs` | Documentación completa |
| `estado-refactorizado` | Estado final del código |

Para comparar las versiones: `git diff estado-inicial estado-refactorizado -- src/main`.

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/diagnostico.md`](docs/diagnostico.md) | Calidad externa vs. interna, deuda técnica, catálogo de code smells, SOLID y Clean Code de la versión inicial |
| [`docs/registro-refactorings.md`](docs/registro-refactorings.md) | Cada commit de refactoring con su técnica, el problema que resuelve y métricas antes/después |
| [`docs/decisiones-tecnicas.md`](docs/decisiones-tecnicas.md) | Decisiones de diseño con contexto, alternativas y consecuencias |
| [`docs/checklist-code-review.md`](docs/checklist-code-review.md) | Revisión técnica del código producido, hallazgos y veredicto |
| [`docs/evidencias/`](docs/evidencias/) | Salidas de las ejecuciones de tests en la versión inicial y en la final |
