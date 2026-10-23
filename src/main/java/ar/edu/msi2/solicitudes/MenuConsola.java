package ar.edu.msi2.solicitudes;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Interfaz de usuario por consola: muestra menus, lee datos, delega en el servicio y muestra
 * los resultados. No contiene reglas de negocio.
 */
public class MenuConsola {

    private final ServicioSolicitudes servicio;
    private final Scanner entrada;
    private final PrintStream salida;

    public MenuConsola(ServicioSolicitudes servicio, Scanner entrada, PrintStream salida) {
        this.servicio = servicio;
        this.entrada = entrada;
        this.salida = salida;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            salida.println();
            salida.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
            salida.println("1. Registrar solicitud");
            salida.println("2. Cambiar estado");
            salida.println("3. Asignar responsable");
            salida.println("4. Buscar / filtrar solicitudes");
            salida.println("5. Listar solicitudes");
            salida.println("6. Reporte");
            salida.println("0. Salir");
            opcion = leerEntero("Opcion: ");
            try {
                ejecutarOpcion(opcion);
            } catch (ReglaDeNegocioException e) {
                salida.println("Error: " + e.getMessage());
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
            salida.println("Hasta luego.");
        } else {
            salida.println("Opcion invalida.");
        }
    }

    private void registrarSolicitud() {
        salida.print("Legajo del estudiante: ");
        String legajo = entrada.nextLine().trim();
        Estudiante estudiante = servicio.buscarEstudiante(legajo);

        salida.println("Tipo de solicitud:");
        salida.println("1. Certificado");
        salida.println("2. Equivalencia");
        salida.println("3. Cambio de comision");
        salida.println("4. Reclamo de nota");
        TipoSolicitud tipo = tipoSegunOpcion(leerEntero("Opcion: "))
                .orElseThrow(() -> new ReglaDeNegocioException("tipo invalido"));

        salida.print("Descripcion: ");
        String descripcion = entrada.nextLine().trim();
        Solicitud.validarDescripcion(descripcion);

        Prioridad prioridad = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "))
                .orElseThrow(() -> new ReglaDeNegocioException("prioridad invalida"));

        Solicitud solicitud = servicio.registrarSolicitud(legajo, tipo, descripcion, prioridad);
        salida.println("Solicitud registrada con ID " + solicitud.getId() + " para " + estudiante.getNombre() + ".");
    }

    private void cambiarEstado() {
        Solicitud solicitud = servicio.buscarSolicitud(leerEntero("ID de la solicitud: "));

        salida.println("Estado actual: " + solicitud.getEstado());
        salida.println("Nuevo estado:");
        salida.println("1. Pendiente");
        salida.println("2. En proceso");
        salida.println("3. Resuelta");
        salida.println("4. Rechazada");
        EstadoSolicitud estadoNuevo = estadoSegunOpcion(leerEntero("Opcion: "))
                .orElseThrow(() -> new ReglaDeNegocioException("estado invalido"));
        solicitud.verificarTransicionA(estadoNuevo);

        EstadoSolicitud estadoAnterior = solicitud.getEstado();
        if (estadoNuevo == EstadoSolicitud.RECHAZADA) {
            salida.print("Motivo del rechazo: ");
            servicio.rechazar(solicitud.getId(), entrada.nextLine().trim());
        } else {
            servicio.cambiarEstado(solicitud.getId(), estadoNuevo);
        }
        salida.println("Solicitud " + solicitud.getId() + ": " + estadoAnterior + " -> " + estadoNuevo);
    }

    private void asignarResponsable() {
        Solicitud solicitud = servicio.buscarSolicitud(leerEntero("ID de la solicitud: "));
        solicitud.verificarQueAdmiteResponsable();

        salida.println("Responsables disponibles:");
        for (Responsable disponible : servicio.listarResponsables()) {
            salida.println(disponible.getId() + ". " + disponible.getNombre() + " (" + disponible.getArea().getNombre() + ")");
        }
        int idResponsable = leerEntero("ID del responsable: ");

        Solicitud actualizada = servicio.asignarResponsable(solicitud.getId(), idResponsable);
        salida.println("Responsable " + actualizada.getResponsable().getNombre() + " asignado a la solicitud " + actualizada.getId() + ".");
    }

    private void buscarSolicitudes() {
        salida.println("Buscar por:");
        salida.println("1. Estado");
        salida.println("2. Tipo");
        salida.println("3. Legajo del estudiante");
        salida.println("4. Prioridad");
        int opcionFiltro = leerEntero("Opcion: ");

        Predicate<Solicitud> criterio;
        if (opcionFiltro == 1) {
            salida.print("Estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
            String estadoBuscado = leerCodigo();
            criterio = solicitud -> solicitud.getEstado().name().equals(estadoBuscado);
        } else if (opcionFiltro == 2) {
            salida.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tipoBuscado = leerCodigo();
            criterio = solicitud -> solicitud.getTipo().name().equals(tipoBuscado);
        } else if (opcionFiltro == 3) {
            salida.print("Legajo: ");
            String legajoBuscado = entrada.nextLine().trim();
            criterio = solicitud -> solicitud.getLegajoEstudiante().equals(legajoBuscado);
        } else if (opcionFiltro == 4) {
            Optional<Prioridad> prioridadBuscada = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "));
            criterio = solicitud -> prioridadBuscada.isPresent() && solicitud.getPrioridad() == prioridadBuscada.get();
        } else {
            salida.println("Opcion invalida.");
            return;
        }
        mostrarResultadoBusqueda(servicio.buscarSolicitudes(criterio));
    }

    private void mostrarResultadoBusqueda(List<Solicitud> encontradas) {
        for (Solicitud solicitud : encontradas) {
            salida.println(formatearSolicitud(solicitud));
        }
        if (encontradas.isEmpty()) {
            salida.println("No se encontraron solicitudes.");
        } else {
            salida.println("Se encontraron " + encontradas.size() + " solicitud(es).");
        }
    }

    private void listarSolicitudes() {
        List<Solicitud> solicitudes = servicio.listarSolicitudes();
        if (solicitudes.isEmpty()) {
            salida.println("No hay solicitudes registradas.");
            return;
        }
        salida.println("Listado de solicitudes (" + solicitudes.size() + "):");
        for (Solicitud solicitud : solicitudes) {
            salida.println(formatearSolicitud(solicitud));
        }
    }

    private void mostrarReporte() {
        ReporteSolicitudes reporte = servicio.generarReporte();
        salida.println("===== REPORTE DE SOLICITUDES =====");
        salida.println("Total de solicitudes: " + reporte.total());
        imprimirConteo("Por estado:", reporte.porEstado());
        imprimirConteo("Por tipo:", reporte.porTipo());
        imprimirConteo("Por prioridad:", reporte.porPrioridad());
        salida.println("Porcentaje de resueltas: " + reporte.porcentajeResueltas() + "%");
        salida.println("Prioridad ALTA sin resolver: " + reporte.altaSinResolver());
        salida.println("Sin responsable asignado: " + reporte.sinResponsable());
    }

    private void imprimirConteo(String titulo, Map<? extends Enum<?>, Long> conteo) {
        salida.println(titulo);
        for (Map.Entry<? extends Enum<?>, Long> entrada : conteo.entrySet()) {
            salida.println("  " + entrada.getKey() + ": " + entrada.getValue());
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
        return entrada.nextLine().trim().toUpperCase().replace(" ", "_");
    }

    private int leerEntero(String mensaje) {
        salida.print(mensaje);
        String linea = entrada.nextLine();
        try {
            return Integer.parseInt(linea.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
