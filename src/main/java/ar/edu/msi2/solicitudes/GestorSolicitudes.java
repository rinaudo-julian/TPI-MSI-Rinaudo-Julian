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
    }

    public void registrarSolicitud() {
        System.out.print("Legajo del estudiante: ");
        String legajo = scanner.nextLine().trim();
        Optional<Estudiante> estudianteEncontrado = repositorioEstudiantes.buscarPorLegajo(legajo);
        if (estudianteEncontrado.isEmpty()) {
            System.out.println("Error: no existe un estudiante con legajo " + legajo);
            return;
        }
        Estudiante estudiante = estudianteEncontrado.get();

        System.out.println("Tipo de solicitud:");
        System.out.println("1. Certificado");
        System.out.println("2. Equivalencia");
        System.out.println("3. Cambio de comision");
        System.out.println("4. Reclamo de nota");
        Optional<TipoSolicitud> tipoElegido = tipoSegunOpcion(leerEntero("Opcion: "));
        if (tipoElegido.isEmpty()) {
            System.out.println("Error: tipo invalido");
            return;
        }
        TipoSolicitud tipo = tipoElegido.get();

        System.out.print("Descripcion: ");
        String descripcion = scanner.nextLine().trim();
        if (descripcion.equals("")) {
            System.out.println("Error: la descripcion no puede estar vacia");
            return;
        }
        if (descripcion.length() > 200) {
            System.out.println("Error: la descripcion no puede superar los 200 caracteres");
            return;
        }

        Optional<Prioridad> prioridadElegida = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "));
        if (prioridadElegida.isEmpty()) {
            System.out.println("Error: prioridad invalida");
            return;
        }
        Prioridad prioridad = prioridadElegida.get();

        // los reclamos de nota siempre van con prioridad alta (pedido de secretaria)
        if (tipo == TipoSolicitud.RECLAMO_NOTA) {
            prioridad = Prioridad.ALTA;
        }

        Solicitud solicitud = new Solicitud();
        solicitud.legajoEstudiante = legajo;
        solicitud.tipo = tipo;
        solicitud.descripcion = descripcion;
        solicitud.prioridad = prioridad;
        solicitud.estado = EstadoSolicitud.PENDIENTE;
        repositorioSolicitudes.guardar(solicitud);
        System.out.println("Solicitud registrada con ID " + solicitud.id + " para " + estudiante.nombre + ".");
    }

    public void cambiarEstado() {
        int id = leerEntero("ID de la solicitud: ");
        Optional<Solicitud> solicitudEncontrada = repositorioSolicitudes.buscarPorId(id);
        if (solicitudEncontrada.isEmpty()) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }
        Solicitud solicitud = solicitudEncontrada.get();

        System.out.println("Estado actual: " + solicitud.estado);
        System.out.println("Nuevo estado:");
        System.out.println("1. Pendiente");
        System.out.println("2. En proceso");
        System.out.println("3. Resuelta");
        System.out.println("4. Rechazada");
        Optional<EstadoSolicitud> estadoElegido = estadoSegunOpcion(leerEntero("Opcion: "));
        if (estadoElegido.isEmpty()) {
            System.out.println("Error: estado invalido");
            return;
        }
        EstadoSolicitud estadoNuevo = estadoElegido.get();

        // validar transicion
        boolean transicionValida = false;
        if (solicitud.estado == EstadoSolicitud.PENDIENTE) {
            if (estadoNuevo == EstadoSolicitud.EN_PROCESO || estadoNuevo == EstadoSolicitud.RECHAZADA) {
                transicionValida = true;
            }
        } else if (solicitud.estado == EstadoSolicitud.EN_PROCESO) {
            if (estadoNuevo == EstadoSolicitud.RESUELTA || estadoNuevo == EstadoSolicitud.RECHAZADA) {
                transicionValida = true;
            }
        }
        if (!transicionValida) {
            System.out.println("Error: no se puede pasar de " + solicitud.estado + " a " + estadoNuevo);
            return;
        }

        if (estadoNuevo == EstadoSolicitud.EN_PROCESO && solicitud.responsable == null) {
            System.out.println("Error: la solicitud debe tener un responsable asignado para pasar a EN_PROCESO");
            return;
        }

        if (estadoNuevo == EstadoSolicitud.RECHAZADA) {
            System.out.print("Motivo del rechazo: ");
            String motivo = scanner.nextLine().trim();
            if (motivo.equals("")) {
                System.out.println("Error: el motivo del rechazo es obligatorio");
                return;
            }
            solicitud.motivoRechazo = motivo;
        }

        EstadoSolicitud estadoAnterior = solicitud.estado;
        solicitud.estado = estadoNuevo;
        System.out.println("Solicitud " + solicitud.id + ": " + estadoAnterior + " -> " + estadoNuevo);
    }

    public void asignarResponsable() {
        int id = leerEntero("ID de la solicitud: ");
        Optional<Solicitud> solicitudEncontrada = repositorioSolicitudes.buscarPorId(id);
        if (solicitudEncontrada.isEmpty()) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }
        Solicitud solicitud = solicitudEncontrada.get();
        if (solicitud.estado == EstadoSolicitud.RESUELTA || solicitud.estado == EstadoSolicitud.RECHAZADA) {
            System.out.println("Error: no se puede asignar responsable a una solicitud cerrada");
            return;
        }

        System.out.println("Responsables disponibles:");
        for (Responsable disponible : repositorioResponsables.buscarTodos()) {
            System.out.println(disponible.id + ". " + disponible.nombre + " (" + disponible.area.getNombre() + ")");
        }
        int idResponsable = leerEntero("ID del responsable: ");
        Optional<Responsable> responsableEncontrado = repositorioResponsables.buscarPorId(idResponsable);
        if (responsableEncontrado.isEmpty()) {
            System.out.println("Error: no existe el responsable " + idResponsable);
            return;
        }
        Responsable responsable = responsableEncontrado.get();

        // cada area atiende ciertos tipos
        Area areaNecesaria;
        if (solicitud.tipo == TipoSolicitud.CERTIFICADO || solicitud.tipo == TipoSolicitud.CAMBIO_COMISION) {
            areaNecesaria = Area.ALUMNADO;
        } else {
            areaNecesaria = Area.DEPARTAMENTO_ACADEMICO;
        }
        if (responsable.area != areaNecesaria) {
            System.out.println("Error: las solicitudes de tipo " + solicitud.tipo + " las atiende el area " + areaNecesaria.getNombre());
            return;
        }

        solicitud.responsable = responsable;
        System.out.println("Responsable " + responsable.nombre + " asignado a la solicitud " + solicitud.id + ".");
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
            criterio = solicitud -> solicitud.estado.name().equals(estadoBuscado);
        } else if (opcionFiltro == 2) {
            System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tipoBuscado = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            criterio = solicitud -> solicitud.tipo.name().equals(tipoBuscado);
        } else if (opcionFiltro == 3) {
            System.out.print("Legajo: ");
            String legajoBuscado = scanner.nextLine().trim();
            criterio = solicitud -> solicitud.legajoEstudiante.equals(legajoBuscado);
        } else if (opcionFiltro == 4) {
            Optional<Prioridad> prioridadBuscada = prioridadSegunOpcion(leerEntero("Prioridad (1=Alta, 2=Media, 3=Baja): "));
            criterio = solicitud -> prioridadBuscada.isPresent() && solicitud.prioridad == prioridadBuscada.get();
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
        if (solicitudes.size() == 0) {
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
            if (solicitud.estado == EstadoSolicitud.PENDIENTE) pendientes++;
            else if (solicitud.estado == EstadoSolicitud.EN_PROCESO) enProceso++;
            else if (solicitud.estado == EstadoSolicitud.RESUELTA) resueltas++;
            else if (solicitud.estado == EstadoSolicitud.RECHAZADA) rechazadas++;

            if (solicitud.tipo == TipoSolicitud.CERTIFICADO) certificados++;
            else if (solicitud.tipo == TipoSolicitud.EQUIVALENCIA) equivalencias++;
            else if (solicitud.tipo == TipoSolicitud.CAMBIO_COMISION) cambiosComision++;
            else if (solicitud.tipo == TipoSolicitud.RECLAMO_NOTA) reclamosNota++;

            if (solicitud.prioridad == Prioridad.ALTA) prioridadAlta++;
            else if (solicitud.prioridad == Prioridad.MEDIA) prioridadMedia++;
            else prioridadBaja++;

            if (solicitud.prioridad == Prioridad.ALTA && (solicitud.estado == EstadoSolicitud.PENDIENTE || solicitud.estado == EstadoSolicitud.EN_PROCESO)) {
                altaSinResolver++;
            }
            if (solicitud.responsable == null) {
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
        String nombreEstudiante = repositorioEstudiantes.buscarPorLegajo(solicitud.legajoEstudiante)
                .map(estudiante -> estudiante.nombre)
                .orElse("");
        String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                + " | Prioridad: " + solicitud.prioridad + " | Estado: " + solicitud.estado
                + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
        if (solicitud.estado == EstadoSolicitud.RECHAZADA) {
            linea = linea + " | Motivo: " + solicitud.motivoRechazo;
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
