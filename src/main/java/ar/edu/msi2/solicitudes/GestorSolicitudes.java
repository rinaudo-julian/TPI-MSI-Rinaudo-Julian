package ar.edu.msi2.solicitudes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

public class GestorSolicitudes {

    private final ServicioSolicitudes servicio;
    Scanner scanner = new Scanner(System.in);

    public GestorSolicitudes(ServicioSolicitudes servicio) {
        this.servicio = servicio;
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
            mostrarReporte();
        } else if (opcion == 0) {
            System.out.println("Hasta luego.");
        } else {
            System.out.println("Opcion invalida.");
        }
    }

    private void registrarSolicitud() {
        System.out.print("Legajo del estudiante: ");
        String legajo = scanner.nextLine().trim();
        Estudiante estudiante = servicio.buscarEstudiante(legajo);

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

        Prioridad prioridad = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "))
                .orElseThrow(() -> new ReglaDeNegocioException("prioridad invalida"));

        Solicitud solicitud = servicio.registrarSolicitud(legajo, tipo, descripcion, prioridad);
        System.out.println("Solicitud registrada con ID " + solicitud.getId() + " para " + estudiante.getNombre() + ".");
    }

    private void cambiarEstado() {
        Solicitud solicitud = servicio.buscarSolicitud(leerEntero("ID de la solicitud: "));

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
            servicio.rechazar(solicitud.getId(), scanner.nextLine().trim());
        } else {
            servicio.cambiarEstado(solicitud.getId(), estadoNuevo);
        }
        System.out.println("Solicitud " + solicitud.getId() + ": " + estadoAnterior + " -> " + estadoNuevo);
    }

    private void asignarResponsable() {
        Solicitud solicitud = servicio.buscarSolicitud(leerEntero("ID de la solicitud: "));
        solicitud.verificarQueAdmiteResponsable();

        System.out.println("Responsables disponibles:");
        for (Responsable disponible : servicio.listarResponsables()) {
            System.out.println(disponible.getId() + ". " + disponible.getNombre() + " (" + disponible.getArea().getNombre() + ")");
        }
        int idResponsable = leerEntero("ID del responsable: ");

        Solicitud actualizada = servicio.asignarResponsable(solicitud.getId(), idResponsable);
        System.out.println("Responsable " + actualizada.getResponsable().getNombre() + " asignado a la solicitud " + actualizada.getId() + ".");
    }

    private void buscarSolicitudes() {
        System.out.println("Buscar por:");
        System.out.println("1. Estado");
        System.out.println("2. Tipo");
        System.out.println("3. Legajo del estudiante");
        System.out.println("4. Prioridad");
        int opcionFiltro = leerEntero("Opcion: ");

        Predicate<Solicitud> criterio;
        if (opcionFiltro == 1) {
            System.out.print("Estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
            String estadoBuscado = leerCodigo();
            criterio = solicitud -> solicitud.getEstado().name().equals(estadoBuscado);
        } else if (opcionFiltro == 2) {
            System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tipoBuscado = leerCodigo();
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
        mostrarResultadoBusqueda(servicio.buscarSolicitudes(criterio));
    }

    private void mostrarResultadoBusqueda(List<Solicitud> encontradas) {
        for (Solicitud solicitud : encontradas) {
            System.out.println(formatearSolicitud(solicitud));
        }
        if (encontradas.isEmpty()) {
            System.out.println("No se encontraron solicitudes.");
        } else {
            System.out.println("Se encontraron " + encontradas.size() + " solicitud(es).");
        }
    }

    private void listarSolicitudes() {
        List<Solicitud> solicitudes = servicio.listarSolicitudes();
        if (solicitudes.isEmpty()) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        System.out.println("Listado de solicitudes (" + solicitudes.size() + "):");
        for (Solicitud solicitud : solicitudes) {
            System.out.println(formatearSolicitud(solicitud));
        }
    }

    private void mostrarReporte() {
        ReporteSolicitudes reporte = servicio.generarReporte();
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total de solicitudes: " + reporte.total());
        imprimirConteo("Por estado:", reporte.porEstado());
        imprimirConteo("Por tipo:", reporte.porTipo());
        imprimirConteo("Por prioridad:", reporte.porPrioridad());
        System.out.println("Porcentaje de resueltas: " + reporte.porcentajeResueltas() + "%");
        System.out.println("Prioridad ALTA sin resolver: " + reporte.altaSinResolver());
        System.out.println("Sin responsable asignado: " + reporte.sinResponsable());
    }

    private void imprimirConteo(String titulo, Map<? extends Enum<?>, Long> conteo) {
        System.out.println(titulo);
        for (Map.Entry<? extends Enum<?>, Long> entrada : conteo.entrySet()) {
            System.out.println("  " + entrada.getKey() + ": " + entrada.getValue());
        }
    }

    private String formatearSolicitud(Solicitud solicitud) {
        String nombreEstudiante = servicio.estudianteDe(solicitud)
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

    /**
     * Lee un texto y lo normaliza al formato de los nombres de los enums: "en proceso" -> "EN_PROCESO".
     */
    private String leerCodigo() {
        return scanner.nextLine().trim().toUpperCase().replace(" ", "_");
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
