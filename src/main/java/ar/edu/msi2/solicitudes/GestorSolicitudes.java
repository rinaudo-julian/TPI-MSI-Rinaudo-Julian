package ar.edu.msi2.solicitudes;

import java.util.ArrayList;
import java.util.Scanner;

public class GestorSolicitudes {

    public ArrayList<Solicitud> solicitudes = new ArrayList<>();
    public ArrayList<Estudiante> estudiantes = new ArrayList<>();
    public ArrayList<Responsable> responsables = new ArrayList<>();
    int ultimoId = 0;
    Scanner scanner = new Scanner(System.in);

    public void cargarDatos() {
        estudiantes.add(new Estudiante("1001", "Ana Gomez", "ana.gomez@mail.com", "Ingenieria en Sistemas"));
        estudiantes.add(new Estudiante("1002", "Bruno Diaz", "bruno.diaz@mail.com", "Ingenieria en Sistemas"));
        estudiantes.add(new Estudiante("1003", "Carla Perez", "carla.perez@mail.com", "Licenciatura en Administracion"));

        responsables.add(new Responsable(1, "Laura Fernandez", "ALUMNADO"));
        responsables.add(new Responsable(2, "Marcos Ruiz", "ALUMNADO"));
        responsables.add(new Responsable(3, "Silvia Torres", "DEPARTAMENTO ACADEMICO"));

        // solicitudes de ejemplo
        Solicitud certificado = new Solicitud();
        ultimoId++;
        certificado.id = ultimoId;
        certificado.legajoEstudiante = "1001";
        certificado.tipo = "CERTIFICADO";
        certificado.descripcion = "Certificado de alumno regular";
        certificado.prioridad = 3;
        certificado.estado = "PENDIENTE";
        solicitudes.add(certificado);

        Solicitud equivalencia = new Solicitud();
        ultimoId++;
        equivalencia.id = ultimoId;
        equivalencia.legajoEstudiante = "1002";
        equivalencia.tipo = "EQUIVALENCIA";
        equivalencia.descripcion = "Equivalencia de Algebra";
        equivalencia.prioridad = 2;
        equivalencia.estado = "EN_PROCESO";
        equivalencia.responsable = responsables.get(2);
        solicitudes.add(equivalencia);

        Solicitud reclamoNota = new Solicitud();
        ultimoId++;
        reclamoNota.id = ultimoId;
        reclamoNota.legajoEstudiante = "1003";
        reclamoNota.tipo = "RECLAMO_NOTA";
        reclamoNota.descripcion = "Revision de nota del parcial de Contabilidad";
        reclamoNota.prioridad = 1;
        reclamoNota.estado = "PENDIENTE";
        solicitudes.add(reclamoNota);

        Solicitud cambioComision = new Solicitud();
        ultimoId++;
        cambioComision.id = ultimoId;
        cambioComision.legajoEstudiante = "1001";
        cambioComision.tipo = "CAMBIO_COMISION";
        cambioComision.descripcion = "Cambio a comision noche";
        cambioComision.prioridad = 2;
        cambioComision.estado = "RESUELTA";
        cambioComision.responsable = responsables.get(0);
        solicitudes.add(cambioComision);
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
            System.out.print("Opcion: ");
            String entrada = scanner.nextLine();
            try {
                opcion = Integer.parseInt(entrada.trim());
            } catch (Exception e) {
                opcion = -1;
            }
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
        Estudiante estudiante = null;
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).legajo.equals(legajo)) {
                estudiante = estudiantes.get(i);
            }
        }
        if (estudiante == null) {
            System.out.println("Error: no existe un estudiante con legajo " + legajo);
            return;
        }

        System.out.println("Tipo de solicitud:");
        System.out.println("1. Certificado");
        System.out.println("2. Equivalencia");
        System.out.println("3. Cambio de comision");
        System.out.println("4. Reclamo de nota");
        System.out.print("Opcion: ");
        int opcionTipo;
        try {
            opcionTipo = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            opcionTipo = -1;
        }
        String tipo = "";
        if (opcionTipo == 1) {
            tipo = "CERTIFICADO";
        } else if (opcionTipo == 2) {
            tipo = "EQUIVALENCIA";
        } else if (opcionTipo == 3) {
            tipo = "CAMBIO_COMISION";
        } else if (opcionTipo == 4) {
            tipo = "RECLAMO_NOTA";
        } else {
            System.out.println("Error: tipo invalido");
            return;
        }

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

        System.out.print("Prioridad (1=Alta, 2=Media, 3=Baja): ");
        int prioridad;
        try {
            prioridad = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            prioridad = -1;
        }
        if (prioridad < 1 || prioridad > 3) {
            System.out.println("Error: prioridad invalida");
            return;
        }

        // los reclamos de nota siempre van con prioridad alta (pedido de secretaria)
        if (tipo.equals("RECLAMO_NOTA")) {
            prioridad = 1;
        }

        Solicitud solicitud = new Solicitud();
        ultimoId++;
        solicitud.id = ultimoId;
        solicitud.legajoEstudiante = legajo;
        solicitud.tipo = tipo;
        solicitud.descripcion = descripcion;
        solicitud.prioridad = prioridad;
        solicitud.estado = "PENDIENTE";
        solicitudes.add(solicitud);
        System.out.println("Solicitud registrada con ID " + solicitud.id + " para " + estudiante.nombre + ".");
    }

    public void cambiarEstado() {
        System.out.print("ID de la solicitud: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            id = -1;
        }
        Solicitud solicitud = null;
        for (Solicitud candidata : solicitudes) {
            if (candidata.id == id) {
                solicitud = candidata;
                break;
            }
        }
        if (solicitud == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }

        System.out.println("Estado actual: " + solicitud.estado);
        System.out.println("Nuevo estado:");
        System.out.println("1. Pendiente");
        System.out.println("2. En proceso");
        System.out.println("3. Resuelta");
        System.out.println("4. Rechazada");
        System.out.print("Opcion: ");
        int opcionEstado;
        try {
            opcionEstado = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            opcionEstado = -1;
        }
        String estadoNuevo;
        switch (opcionEstado) {
            case 1:
                estadoNuevo = "PENDIENTE";
                break;
            case 2:
                estadoNuevo = "EN_PROCESO";
                break;
            case 3:
                estadoNuevo = "RESUELTA";
                break;
            case 4:
                estadoNuevo = "RECHAZADA";
                break;
            default:
                System.out.println("Error: estado invalido");
                return;
        }

        // validar transicion
        boolean transicionValida = false;
        if (solicitud.estado.equals("PENDIENTE")) {
            if (estadoNuevo.equals("EN_PROCESO") || estadoNuevo.equals("RECHAZADA")) {
                transicionValida = true;
            }
        } else if (solicitud.estado.equals("EN_PROCESO")) {
            if (estadoNuevo.equals("RESUELTA") || estadoNuevo.equals("RECHAZADA")) {
                transicionValida = true;
            }
        } else if (solicitud.estado.equals("RESUELTA") || solicitud.estado.equals("RECHAZADA")) {
            transicionValida = false;
        }
        if (!transicionValida) {
            System.out.println("Error: no se puede pasar de " + solicitud.estado + " a " + estadoNuevo);
            return;
        }

        if (estadoNuevo.equals("EN_PROCESO") && solicitud.responsable == null) {
            System.out.println("Error: la solicitud debe tener un responsable asignado para pasar a EN_PROCESO");
            return;
        }

        if (estadoNuevo.equals("RECHAZADA")) {
            System.out.print("Motivo del rechazo: ");
            String motivo = scanner.nextLine().trim();
            if (motivo.equals("")) {
                System.out.println("Error: el motivo del rechazo es obligatorio");
                return;
            }
            solicitud.motivoRechazo = motivo;
        }

        String estadoAnterior = solicitud.estado;
        solicitud.estado = estadoNuevo;
        System.out.println("Solicitud " + solicitud.id + ": " + estadoAnterior + " -> " + estadoNuevo);
    }

    public void asignarResponsable() {
        System.out.print("ID de la solicitud: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            id = -1;
        }
        Solicitud solicitud = null;
        for (Solicitud candidata : solicitudes) {
            if (candidata.id == id) {
                solicitud = candidata;
                break;
            }
        }
        if (solicitud == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }
        if (solicitud.estado.equals("RESUELTA") || solicitud.estado.equals("RECHAZADA")) {
            System.out.println("Error: no se puede asignar responsable a una solicitud cerrada");
            return;
        }

        System.out.println("Responsables disponibles:");
        for (Responsable disponible : responsables) {
            System.out.println(disponible.id + ". " + disponible.nombre + " (" + disponible.area + ")");
        }
        System.out.print("ID del responsable: ");
        int idResponsable;
        try {
            idResponsable = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            idResponsable = -1;
        }
        Responsable responsable = null;
        for (Responsable candidato : responsables) {
            if (candidato.id == idResponsable) {
                responsable = candidato;
            }
        }
        if (responsable == null) {
            System.out.println("Error: no existe el responsable " + idResponsable);
            return;
        }

        // cada area atiende ciertos tipos
        String areaNecesaria;
        if (solicitud.tipo.equals("CERTIFICADO") || solicitud.tipo.equals("CAMBIO_COMISION")) {
            areaNecesaria = "ALUMNADO";
        } else {
            areaNecesaria = "DEPARTAMENTO ACADEMICO";
        }
        if (!responsable.area.equals(areaNecesaria)) {
            System.out.println("Error: las solicitudes de tipo " + solicitud.tipo + " las atiende el area " + areaNecesaria);
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
        System.out.print("Opcion: ");
        int opcionFiltro;
        try {
            opcionFiltro = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            opcionFiltro = -1;
        }

        int encontradas = 0;
        if (opcionFiltro == 1) {
            System.out.print("Estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
            String estadoBuscado = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            for (Solicitud solicitud : solicitudes) {
                if (solicitud.estado.equals(estadoBuscado)) {
                    String nombreEstudiante = "";
                    for (Estudiante estudiante : estudiantes) {
                        if (estudiante.legajo.equals(solicitud.legajoEstudiante)) {
                            nombreEstudiante = estudiante.nombre;
                        }
                    }
                    String etiquetaPrioridad;
                    if (solicitud.prioridad == 1) {
                        etiquetaPrioridad = "ALTA";
                    } else if (solicitud.prioridad == 2) {
                        etiquetaPrioridad = "MEDIA";
                    } else {
                        etiquetaPrioridad = "BAJA";
                    }
                    String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                            + " | Prioridad: " + etiquetaPrioridad + " | Estado: " + solicitud.estado
                            + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
                    if (solicitud.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + solicitud.motivoRechazo;
                    }
                    System.out.println(linea);
                    encontradas++;
                }
            }
        } else if (opcionFiltro == 2) {
            System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tipoBuscado = scanner.nextLine().trim().toUpperCase().replace(" ", "_");
            for (Solicitud solicitud : solicitudes) {
                if (solicitud.tipo.equals(tipoBuscado)) {
                    String nombreEstudiante = "";
                    for (Estudiante estudiante : estudiantes) {
                        if (estudiante.legajo.equals(solicitud.legajoEstudiante)) {
                            nombreEstudiante = estudiante.nombre;
                        }
                    }
                    String etiquetaPrioridad;
                    if (solicitud.prioridad == 1) {
                        etiquetaPrioridad = "ALTA";
                    } else if (solicitud.prioridad == 2) {
                        etiquetaPrioridad = "MEDIA";
                    } else {
                        etiquetaPrioridad = "BAJA";
                    }
                    String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                            + " | Prioridad: " + etiquetaPrioridad + " | Estado: " + solicitud.estado
                            + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
                    if (solicitud.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + solicitud.motivoRechazo;
                    }
                    System.out.println(linea);
                    encontradas++;
                }
            }
        } else if (opcionFiltro == 3) {
            System.out.print("Legajo: ");
            String legajoBuscado = scanner.nextLine().trim();
            for (Solicitud solicitud : solicitudes) {
                if (solicitud.legajoEstudiante.equals(legajoBuscado)) {
                    String nombreEstudiante = "";
                    for (Estudiante estudiante : estudiantes) {
                        if (estudiante.legajo.equals(solicitud.legajoEstudiante)) {
                            nombreEstudiante = estudiante.nombre;
                        }
                    }
                    String etiquetaPrioridad;
                    if (solicitud.prioridad == 1) {
                        etiquetaPrioridad = "ALTA";
                    } else if (solicitud.prioridad == 2) {
                        etiquetaPrioridad = "MEDIA";
                    } else {
                        etiquetaPrioridad = "BAJA";
                    }
                    String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                            + " | Prioridad: " + etiquetaPrioridad + " | Estado: " + solicitud.estado
                            + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
                    if (solicitud.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + solicitud.motivoRechazo;
                    }
                    System.out.println(linea);
                    encontradas++;
                }
            }
        } else if (opcionFiltro == 4) {
            System.out.print("Prioridad (1=Alta, 2=Media, 3=Baja): ");
            int prioridadBuscada;
            try {
                prioridadBuscada = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                prioridadBuscada = -1;
            }
            for (Solicitud solicitud : solicitudes) {
                if (solicitud.prioridad == prioridadBuscada) {
                    String nombreEstudiante = "";
                    for (Estudiante estudiante : estudiantes) {
                        if (estudiante.legajo.equals(solicitud.legajoEstudiante)) {
                            nombreEstudiante = estudiante.nombre;
                        }
                    }
                    String etiquetaPrioridad;
                    if (solicitud.prioridad == 1) {
                        etiquetaPrioridad = "ALTA";
                    } else if (solicitud.prioridad == 2) {
                        etiquetaPrioridad = "MEDIA";
                    } else {
                        etiquetaPrioridad = "BAJA";
                    }
                    String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                            + " | Prioridad: " + etiquetaPrioridad + " | Estado: " + solicitud.estado
                            + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
                    if (solicitud.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + solicitud.motivoRechazo;
                    }
                    System.out.println(linea);
                    encontradas++;
                }
            }
        } else {
            System.out.println("Opcion invalida.");
            return;
        }
        if (encontradas == 0) {
            System.out.println("No se encontraron solicitudes.");
        } else {
            System.out.println("Se encontraron " + encontradas + " solicitud(es).");
        }
    }

    public void listarSolicitudes() {
        if (solicitudes.size() == 0) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        System.out.println("Listado de solicitudes (" + solicitudes.size() + "):");
        for (Solicitud solicitud : solicitudes) {
            String nombreEstudiante = "";
            for (Estudiante estudiante : estudiantes) {
                if (estudiante.legajo.equals(solicitud.legajoEstudiante)) {
                    nombreEstudiante = estudiante.nombre;
                }
            }
            String etiquetaPrioridad;
            if (solicitud.prioridad == 1) {
                etiquetaPrioridad = "ALTA";
            } else if (solicitud.prioridad == 2) {
                etiquetaPrioridad = "MEDIA";
            } else {
                etiquetaPrioridad = "BAJA";
            }
            String linea = "#" + solicitud.id + " [" + solicitud.tipo + "] " + solicitud.descripcion + " | Estudiante: " + nombreEstudiante + " (" + solicitud.legajoEstudiante + ")"
                    + " | Prioridad: " + etiquetaPrioridad + " | Estado: " + solicitud.estado
                    + " | Responsable: " + (solicitud.responsable == null ? "-" : solicitud.responsable.nombre);
            if (solicitud.estado.equals("RECHAZADA")) {
                linea = linea + " | Motivo: " + solicitud.motivoRechazo;
            }
            System.out.println(linea);
            //System.out.println(s.id + " - " + s.tipo + " - " + s.estado);
        }
    }

    public void generarReporte() {
        int total = solicitudes.size();
        int pendientes = 0, enProceso = 0, resueltas = 0, rechazadas = 0;
        int certificados = 0, equivalencias = 0, cambiosComision = 0, reclamosNota = 0;
        int prioridadAlta = 0, prioridadMedia = 0, prioridadBaja = 0;
        int altaSinResolver = 0;
        int sinResponsable = 0;
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.estado.equals("PENDIENTE")) pendientes++;
            else if (solicitud.estado.equals("EN_PROCESO")) enProceso++;
            else if (solicitud.estado.equals("RESUELTA")) resueltas++;
            else if (solicitud.estado.equals("RECHAZADA")) rechazadas++;

            if (solicitud.tipo.equals("CERTIFICADO")) certificados++;
            else if (solicitud.tipo.equals("EQUIVALENCIA")) equivalencias++;
            else if (solicitud.tipo.equals("CAMBIO_COMISION")) cambiosComision++;
            else if (solicitud.tipo.equals("RECLAMO_NOTA")) reclamosNota++;

            if (solicitud.prioridad == 1) prioridadAlta++;
            else if (solicitud.prioridad == 2) prioridadMedia++;
            else prioridadBaja++;

            if (solicitud.prioridad == 1 && (solicitud.estado.equals("PENDIENTE") || solicitud.estado.equals("EN_PROCESO"))) {
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

    public Solicitud buscarPorId(int id) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).id == id) {
                return solicitudes.get(i);
            }
        }
        return null;
    }
}
