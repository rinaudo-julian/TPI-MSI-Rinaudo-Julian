package ar.edu.msi2.solicitudes;

import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantes;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsables;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudes;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

public class GestorSolicitudes {

    private final RepositorioSolicitudes repositorioSolicitudes;
    private final RepositorioEstudiantes repositorioEstudiantes;
    private final RepositorioResponsables repositorioResponsables;
    Scanner scanner = new Scanner(System.in);

    public GestorSolicitudes(RepositorioSolicitudes repositorioSolicitudes,
                             RepositorioEstudiantes repositorioEstudiantes,
                             RepositorioResponsables repositorioResponsables) {
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioEstudiantes = repositorioEstudiantes;
        this.repositorioResponsables = repositorioResponsables;
    }

    public void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar solicitudes");
            System.out.println("5. Listar solicitudes");
            System.out.println("6. Reporte");
            System.out.println("0. Salir");
            opcion = leerEntero("Opcion: ");
            try {
                ejecutarOpcion(opcion);
            } catch (ReglaDeNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void ejecutarOpcion(int opcion) {
        if (opcion == 1) {
            registrarSolicitud();
        } else if (opcion == 2) {
            cambiarEstado();
        } else if (opcion == 3) {
            asignarResponsable();
        } else if (opcion == 4) {
            buscarSolicitudes();
        } else if (opcion == 5) {
            listarSolicitudes();
        } else if (opcion == 6) {
            generarReporte();
        } else if (opcion == 0) {
            System.out.println("Hasta luego.");
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    public void registrarSolicitud() {
        System.out.print("Legajo del estudiante: ");
        String legajo = scanner.nextLine().trim();
        Estudiante estudiante = repositorioEstudiantes.buscarPorLegajo(legajo)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe un estudiante con legajo " + legajo));

        System.out.println("Tipo de solicitud:");
        System.out.println("1. Certificado");
        System.out.println("2. Equivalencia");
        System.out.println("3. Cambio de comision");
        System.out.println("4. Reclamo de nota");
        TipoSolicitud tipo = tipoSegunOpcion(leerEntero("Opcion: "))
                .orElseThrow(() -> new ReglaDeNegocioException("tipo invalido"));

        System.out.print("Descripcion: ");
        String descripcion = scanner.nextLine().trim();
        Solicitud.validarDescripcion(descripcion);

        Prioridad prioridadIngresada = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "))
                .orElseThrow(() -> new ReglaDeNegocioException("prioridad invalida"));

        Solicitud solicitud = new Solicitud(legajo, tipo, descripcion, prioridadSegunPolitica(tipo, prioridadIngresada));
        repositorioSolicitudes.guardar(solicitud);
        System.out.println("Solicitud registrada con ID " + solicitud.getId() + " para " + estudiante.getNombre() + ".");
    }

    /**
     * Los reclamos de nota siempre van con prioridad alta (pedido de secretaria).
     */
    private Prioridad prioridadSegunPolitica(TipoSolicitud tipo, Prioridad prioridadIngresada) {
        if (tipo == TipoSolicitud.RECLAMO_NOTA) {
            return Prioridad.ALTA;
        }
        return prioridadIngresada;
    }

    public void cambiarEstado() {
        Solicitud solicitud = buscarSolicitud(leerEntero("ID de la solicitud: "));

        System.out.println("Estado actual: " + solicitud.getEstado());
        System.out.println("Nuevo estado:");
        System.out.println("1. Pendiente");
        System.out.println("2. En proceso");
        System.out.println("3. Resuelta");
        System.out.println("4. Rechazada");
        EstadoSolicitud estadoNuevo = estadoSegunOpcion(leerEntero("Opcion: "))
                .orElseThrow(() -> new ReglaDeNegocioException("estado invalido"));
        solicitud.verificarTransicionA(estadoNuevo);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        if (estadoNuevo == EstadoSolicitud.RECHAZADA) {
            System.out.print("Motivo del rechazo: ");
            solicitud.rechazar(scanner.nextLine().trim());
        } else {
            solicitud.cambiarEstado(estadoNuevo);
        }
        System.out.println("Solicitud " + solicitud.getId() + ": " + estadoAnterior + " -> " + estadoNuevo);
    }

    public void asignarResponsable() {
        Solicitud solicitud = buscarSolicitud(leerEntero("ID de la solicitud: "));
        solicitud.verificarQueAdmiteResponsable();

        System.out.println("Responsables disponibles:");
        for (Responsable disponible : repositorioResponsables.buscarTodos()) {
            System.out.println(disponible.getId() + ". " + disponible.getNombre() + " (" + disponible.getArea().getNombre() + ")");
        }
        int idResponsable = leerEntero("ID del responsable: ");
        Responsable responsable = repositorioResponsables.buscarPorId(idResponsable)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe el responsable " + idResponsable));

        solicitud.asignarResponsable(responsable);
        System.out.println("Responsable " + responsable.getNombre() + " asignado a la solicitud " + solicitud.getId() + ".");
    }

    private Solicitud buscarSolicitud(int id) {
        return repositorioSolicitudes.buscarPorId(id)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe la solicitud " + id));
    }

    public void buscarSolicitudes() {
        System.out.println("Buscar por:");
        System.out.println("1. Estado");
        System.out.println("2. Tipo");
        System.out.println("3. Legajo del estudiante");
        System.out.println("4. Prioridad");
        int opcionFiltro = leerEntero("Opcion: ");

        Predicate<Solicitud> criterio;
        if (opcionFiltro == 1) {
            System.out.print("Estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
            String estadoBuscado = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            criterio = solicitud -> solicitud.getEstado().name().equals(estadoBuscado);
        } else if (opcionFiltro == 2) {
            System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tipoBuscado = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            criterio = solicitud -> solicitud.getTipo().name().equals(tipoBuscado);
        } else if (opcionFiltro == 3) {
            System.out.print("Legajo: ");
            String legajoBuscado = scanner.nextLine().trim();
            criterio = solicitud -> solicitud.getLegajoEstudiante().equals(legajoBuscado);
        } else if (opcionFiltro == 4) {
            Optional<Prioridad> prioridadBuscada = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "));
            criterio = solicitud -> prioridadBuscada.isPresent() && solicitud.getPrioridad() == prioridadBuscada.get();
        } else {
            System.out.println("Opcion invalida.");
            return;
        }
        mostrarSolicitudesQueCumplen(criterio);
    }

    private void mostrarSolicitudesQueCumplen(Predicate<Solicitud> criterio) {
        List<Solicitud> encontradas = repositorioSolicitudes.buscarTodas().stream()
                .filter(criterio)
                .toList();
        for (Solicitud solicitud : encontradas) {
            System.out.println(formatearSolicitud(solicitud));
        }
        if (encontradas.isEmpty()) {
            System.out.println("No se encontraron solicitudes.");
        } else {
            System.out.println("Se encontraron " + encontradas.size() + " solicitud(es).");
        }
    }

    public void listarSolicitudes() {
        List<Solicitud> solicitudes = repositorioSolicitudes.buscarTodas();
        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        System.out.println("Listado de solicitudes (" + solicitudes.size() + "):");
        for (Solicitud solicitud : solicitudes) {
            System.out.println(formatearSolicitud(solicitud));
        }
    }

    public void generarReporte() {
        List<Solicitud> solicitudes = repositorioSolicitudes.buscarTodas();
        int total = solicitudes.size();
        int pendientes = 0, enProceso = 0, resueltas = 0, rechazadas = 0;
        int certificados = 0, equivalencias = 0, cambiosComision = 0, reclamosNota = 0;
        int prioridadAlta = 0, prioridadMedia = 0, prioridadBaja = 0;
        int altaSinResolver = 0;
        int sinResponsable = 0;
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) pendientes++;
            else if (solicitud.getEstado() == EstadoSolicitud.EN_PROCESO) enProceso++;
            else if (solicitud.getEstado() == EstadoSolicitud.RESUELTA) resueltas++;
            else if (solicitud.getEstado() == EstadoSolicitud.RECHAZADA) rechazadas++;

            if (solicitud.getTipo() == TipoSolicitud.CERTIFICADO) certificados++;
            else if (solicitud.getTipo() == TipoSolicitud.EQUIVALENCIA) equivalencias++;
            else if (solicitud.getTipo() == TipoSolicitud.CAMBIO_COMISION) cambiosComision++;
            else if (solicitud.getTipo() == TipoSolicitud.RECLAMO_NOTA) reclamosNota++;

            if (solicitud.getPrioridad() == Prioridad.ALTA) prioridadAlta++;
            else if (solicitud.getPrioridad() == Prioridad.MEDIA) prioridadMedia++;
            else prioridadBaja++;

            if (solicitud.esAltaSinResolver()) {
                altaSinResolver++;
            }
            if (!solicitud.tieneResponsable()) {
                sinResponsable++;
            }
        }
        int porcentajeResueltas = 0;
        if (total > 0) {
            porcentajeResueltas = resueltas * 100 / total;
        }
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total de solicitudes: " + total);
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + pendientes);
        System.out.println("  EN_PROCESO: " + enProceso);
        System.out.println("  RESUELTA: " + resueltas);
        System.out.println("  RECHAZADA: " + rechazadas);
        System.out.println("Por tipo:");
        System.out.println("  CERTIFICADO: " + certificados);
        System.out.println("  EQUIVALENCIA: " + equivalencias);
        System.out.println("  CAMBIO_COMISION: " + cambiosComision);
        System.out.println("  RECLAMO_NOTA: " + reclamosNota);
        System.out.println("Por prioridad:");
        System.out.println("  ALTA: " + prioridadAlta);
        System.out.println("  MEDIA: " + prioridadMedia);
        System.out.println("  BAJA: " + prioridadBaja);
        System.out.println("Porcentaje de resueltas: " + porcentajeResueltas + "%");
        System.out.println("Prioridad ALTA sin resolver: " + altaSinResolver);
        System.out.println("Sin responsable asignado: " + sinResponsable);
    }

    private String formatearSolicitud(Solicitud solicitud) {
        String nombreEstudiante = repositorioEstudiantes.buscarPorLegajo(solicitud.getLegajoEstudiante())
                .map(Estudiante::getNombre)
                .orElse("");
        String linea = "#" + solicitud.getId() + " [" + solicitud.getTipo() + "] " + solicitud.getDescripcion()
                + " | Estudiante: " + nombreEstudiante + " (" + solicitud.getLegajoEstudiante() + ")"
                + " | Prioridad: " + solicitud.getPrioridad() + " | Estado: " + solicitud.getEstado()
                + " | Responsable: " + (solicitud.tieneResponsable() ? solicitud.getResponsable().getNombre() : "-");
        if (solicitud.getEstado() == EstadoSolicitud.RECHAZADA) {
            linea = linea + " | Motivo: " + solicitud.getMotivoRechazo();
        }
        return linea;
    }

    private Optional<TipoSolicitud> tipoSegunOpcion(int opcion) {
        return switch (opcion) {
            case 1 -> Optional.of(TipoSolicitud.CERTIFICADO);
            case 2 -> Optional.of(TipoSolicitud.EQUIVALENCIA);
            case 3 -> Optional.of(TipoSolicitud.CAMBIO_COMISION);
            case 4 -> Optional.of(TipoSolicitud.RECLAMO_NOTA);
            default -> Optional.empty();
        };
    }

    private Optional<EstadoSolicitud> estadoSegunOpcion(int opcion) {
        return switch (opcion) {
            case 1 -> Optional.of(EstadoSolicitud.PENDIENTE);
            case 2 -> Optional.of(EstadoSolicitud.EN_PROCESO);
            case 3 -> Optional.of(EstadoSolicitud.RESUELTA);
            case 4 -> Optional.of(EstadoSolicitud.RECHAZADA);
            default -> Optional.empty();
        };
    }

    private Optional<Prioridad> prioridadSegunOpcion(int opcion) {
        return switch (opcion) {
            case 1 -> Optional.of(Prioridad.ALTA);
            case 2 -> Optional.of(Prioridad.MEDIA);
            case 3 -> Optional.of(Prioridad.BAJA);
            default -> Optional.empty();
        };
    }

    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        String entrada = scanner.nextLine();
        try {
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
