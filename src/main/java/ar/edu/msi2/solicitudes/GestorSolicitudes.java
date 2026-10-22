package ar.edu.msi2.solicitudes;

import java.util.ArrayList;
import java.util.Scanner;

// Clase principal del sistema
public class GestorSolicitudes {

    public ArrayList<Solicitud> lista = new ArrayList<>();
    public ArrayList<Estudiante> estudiantes = new ArrayList<>();
    public ArrayList<Responsable> responsables = new ArrayList<>();
    int ultId = 0;
    Scanner sc = new Scanner(System.in);

    public void cargarDatos() {
        estudiantes.add(new Estudiante("1001", "Ana Gomez", "ana.gomez@mail.com", "Ingenieria en Sistemas"));
        estudiantes.add(new Estudiante("1002", "Bruno Diaz", "bruno.diaz@mail.com", "Ingenieria en Sistemas"));
        estudiantes.add(new Estudiante("1003", "Carla Perez", "carla.perez@mail.com", "Licenciatura en Administracion"));

        responsables.add(new Responsable(1, "Laura Fernandez", "ALUMNADO"));
        responsables.add(new Responsable(2, "Marcos Ruiz", "ALUMNADO"));
        responsables.add(new Responsable(3, "Silvia Torres", "DEPARTAMENTO ACADEMICO"));

        // solicitudes de ejemplo
        Solicitud s = new Solicitud();
        ultId++;
        s.id = ultId;
        s.legajo = "1001";
        s.tipo = "CERTIFICADO";
        s.desc = "Certificado de alumno regular";
        s.prioridad = 3;
        s.estado = "PENDIENTE";
        lista.add(s);

        s = new Solicitud();
        ultId++;
        s.id = ultId;
        s.legajo = "1002";
        s.tipo = "EQUIVALENCIA";
        s.desc = "Equivalencia de Algebra";
        s.prioridad = 2;
        s.estado = "EN_PROCESO";
        s.resp = responsables.get(2);
        lista.add(s);

        s = new Solicitud();
        ultId++;
        s.id = ultId;
        s.legajo = "1003";
        s.tipo = "RECLAMO_NOTA";
        s.desc = "Revision de nota del parcial de Contabilidad";
        s.prioridad = 1;
        s.estado = "PENDIENTE";
        lista.add(s);

        s = new Solicitud();
        ultId++;
        s.id = ultId;
        s.legajo = "1001";
        s.tipo = "CAMBIO_COMISION";
        s.desc = "Cambio a comision noche";
        s.prioridad = 2;
        s.estado = "RESUELTA";
        s.resp = responsables.get(0);
        lista.add(s);
    }

    public void menu() {
        int op = -1;
        while (op != 0) {
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
            String linea = sc.nextLine();
            try {
                op = Integer.parseInt(linea.trim());
            } catch (Exception e) {
                op = -1;
            }
            if (op == 1) {
                registrar();
            } else if (op == 2) {
                cambiarEstado();
            } else if (op == 3) {
                asignar();
            } else if (op == 4) {
                buscar();
            } else if (op == 5) {
                listar();
            } else if (op == 6) {
                reporte();
            } else if (op == 0) {
                System.out.println("Hasta luego.");
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }

    public void registrar() {
        System.out.print("Legajo del estudiante: ");
        String leg = sc.nextLine().trim();
        Estudiante e = null;
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).legajo.equals(leg)) {
                e = estudiantes.get(i);
            }
        }
        if (e == null) {
            System.out.println("Error: no existe un estudiante con legajo " + leg);
            return;
        }

        System.out.println("Tipo de solicitud:");
        System.out.println("1. Certificado");
        System.out.println("2. Equivalencia");
        System.out.println("3. Cambio de comision");
        System.out.println("4. Reclamo de nota");
        System.out.print("Opcion: ");
        int t;
        try {
            t = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            t = -1;
        }
        String tipo = "";
        if (t == 1) {
            tipo = "CERTIFICADO";
        } else if (t == 2) {
            tipo = "EQUIVALENCIA";
        } else if (t == 3) {
            tipo = "CAMBIO_COMISION";
        } else if (t == 4) {
            tipo = "RECLAMO_NOTA";
        } else {
            System.out.println("Error: tipo invalido");
            return;
        }

        System.out.print("Descripcion: ");
        String d = sc.nextLine().trim();
        if (d.equals("")) {
            System.out.println("Error: la descripcion no puede estar vacia");
            return;
        }
        if (d.length() > 200) {
            System.out.println("Error: la descripcion no puede superar los 200 caracteres");
            return;
        }

        System.out.print("Prioridad (1=Alta, 2=Media, 3=Baja): ");
        int p;
        try {
            p = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            p = -1;
        }
        if (p < 1 || p > 3) {
            System.out.println("Error: prioridad invalida");
            return;
        }

        // los reclamos de nota siempre van con prioridad alta (pedido de secretaria)
        if (tipo.equals("RECLAMO_NOTA")) {
            p = 1;
        }

        Solicitud s = new Solicitud();
        ultId++;
        s.id = ultId;
        s.legajo = leg;
        s.tipo = tipo;
        s.desc = d;
        s.prioridad = p;
        s.estado = "PENDIENTE";
        lista.add(s);
        System.out.println("Solicitud registrada con ID " + s.id + " para " + e.nombre + ".");
    }

    public void cambiarEstado() {
        System.out.print("ID de la solicitud: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            id = -1;
        }
        Solicitud s = null;
        for (Solicitud x : lista) {
            if (x.id == id) {
                s = x;
                break;
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }

        System.out.println("Estado actual: " + s.estado);
        System.out.println("Nuevo estado:");
        System.out.println("1. Pendiente");
        System.out.println("2. En proceso");
        System.out.println("3. Resuelta");
        System.out.println("4. Rechazada");
        System.out.print("Opcion: ");
        int op;
        try {
            op = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            op = -1;
        }
        String nuevo;
        switch (op) {
            case 1:
                nuevo = "PENDIENTE";
                break;
            case 2:
                nuevo = "EN_PROCESO";
                break;
            case 3:
                nuevo = "RESUELTA";
                break;
            case 4:
                nuevo = "RECHAZADA";
                break;
            default:
                System.out.println("Error: estado invalido");
                return;
        }

        // validar transicion
        boolean ok = false;
        if (s.estado.equals("PENDIENTE")) {
            if (nuevo.equals("EN_PROCESO") || nuevo.equals("RECHAZADA")) {
                ok = true;
            }
        } else if (s.estado.equals("EN_PROCESO")) {
            if (nuevo.equals("RESUELTA") || nuevo.equals("RECHAZADA")) {
                ok = true;
            }
        } else if (s.estado.equals("RESUELTA") || s.estado.equals("RECHAZADA")) {
            ok = false;
        }
        if (!ok) {
            System.out.println("Error: no se puede pasar de " + s.estado + " a " + nuevo);
            return;
        }

        if (nuevo.equals("EN_PROCESO") && s.resp == null) {
            System.out.println("Error: la solicitud debe tener un responsable asignado para pasar a EN_PROCESO");
            return;
        }

        if (nuevo.equals("RECHAZADA")) {
            System.out.print("Motivo del rechazo: ");
            String m = sc.nextLine().trim();
            if (m.equals("")) {
                System.out.println("Error: el motivo del rechazo es obligatorio");
                return;
            }
            s.obs = m;
        }

        String ant = s.estado;
        s.estado = nuevo;
        System.out.println("Solicitud " + s.id + ": " + ant + " -> " + nuevo);
    }

    public void asignar() {
        System.out.print("ID de la solicitud: ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            id = -1;
        }
        Solicitud s = null;
        for (Solicitud x : lista) {
            if (x.id == id) {
                s = x;
                break;
            }
        }
        if (s == null) {
            System.out.println("Error: no existe la solicitud " + id);
            return;
        }
        if (s.estado.equals("RESUELTA") || s.estado.equals("RECHAZADA")) {
            System.out.println("Error: no se puede asignar responsable a una solicitud cerrada");
            return;
        }

        System.out.println("Responsables disponibles:");
        for (Responsable r : responsables) {
            System.out.println(r.id + ". " + r.nombre + " (" + r.area + ")");
        }
        System.out.print("ID del responsable: ");
        int rid;
        try {
            rid = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            rid = -1;
        }
        Responsable r = null;
        for (Responsable aux : responsables) {
            if (aux.id == rid) {
                r = aux;
            }
        }
        if (r == null) {
            System.out.println("Error: no existe el responsable " + rid);
            return;
        }

        // cada area atiende ciertos tipos
        String areaNecesaria;
        if (s.tipo.equals("CERTIFICADO") || s.tipo.equals("CAMBIO_COMISION")) {
            areaNecesaria = "ALUMNADO";
        } else {
            areaNecesaria = "DEPARTAMENTO ACADEMICO";
        }
        if (!r.area.equals(areaNecesaria)) {
            System.out.println("Error: las solicitudes de tipo " + s.tipo + " las atiende el area " + areaNecesaria);
            return;
        }

        s.resp = r;
        System.out.println("Responsable " + r.nombre + " asignado a la solicitud " + s.id + ".");
    }

    public void buscar() {
        System.out.println("Buscar por:");
        System.out.println("1. Estado");
        System.out.println("2. Tipo");
        System.out.println("3. Legajo del estudiante");
        System.out.println("4. Prioridad");
        System.out.print("Opcion: ");
        int op;
        try {
            op = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception ex) {
            op = -1;
        }

        int cont = 0;
        if (op == 1) {
            System.out.print("Estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
            String est = sc.nextLine().trim().toUpperCase().replace(" ", "_");
            for (Solicitud s : lista) {
                if (s.estado.equals(est)) {
                    String nom = "";
                    for (Estudiante e : estudiantes) {
                        if (e.legajo.equals(s.legajo)) {
                            nom = e.nombre;
                        }
                    }
                    String pr;
                    if (s.prioridad == 1) {
                        pr = "ALTA";
                    } else if (s.prioridad == 2) {
                        pr = "MEDIA";
                    } else {
                        pr = "BAJA";
                    }
                    String linea = "#" + s.id + " [" + s.tipo + "] " + s.desc + " | Estudiante: " + nom + " (" + s.legajo + ")"
                            + " | Prioridad: " + pr + " | Estado: " + s.estado
                            + " | Responsable: " + (s.resp == null ? "-" : s.resp.nombre);
                    if (s.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + s.obs;
                    }
                    System.out.println(linea);
                    cont++;
                }
            }
        } else if (op == 2) {
            System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, CAMBIO_COMISION, RECLAMO_NOTA): ");
            String tip = sc.nextLine().trim().toUpperCase().replace(" ", "_");
            for (Solicitud s : lista) {
                if (s.tipo.equals(tip)) {
                    String nom = "";
                    for (Estudiante e : estudiantes) {
                        if (e.legajo.equals(s.legajo)) {
                            nom = e.nombre;
                        }
                    }
                    String pr;
                    if (s.prioridad == 1) {
                        pr = "ALTA";
                    } else if (s.prioridad == 2) {
                        pr = "MEDIA";
                    } else {
                        pr = "BAJA";
                    }
                    String linea = "#" + s.id + " [" + s.tipo + "] " + s.desc + " | Estudiante: " + nom + " (" + s.legajo + ")"
                            + " | Prioridad: " + pr + " | Estado: " + s.estado
                            + " | Responsable: " + (s.resp == null ? "-" : s.resp.nombre);
                    if (s.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + s.obs;
                    }
                    System.out.println(linea);
                    cont++;
                }
            }
        } else if (op == 3) {
            System.out.print("Legajo: ");
            String leg = sc.nextLine().trim();
            for (Solicitud s : lista) {
                if (s.legajo.equals(leg)) {
                    String nom = "";
                    for (Estudiante e : estudiantes) {
                        if (e.legajo.equals(s.legajo)) {
                            nom = e.nombre;
                        }
                    }
                    String pr;
                    if (s.prioridad == 1) {
                        pr = "ALTA";
                    } else if (s.prioridad == 2) {
                        pr = "MEDIA";
                    } else {
                        pr = "BAJA";
                    }
                    String linea = "#" + s.id + " [" + s.tipo + "] " + s.desc + " | Estudiante: " + nom + " (" + s.legajo + ")"
                            + " | Prioridad: " + pr + " | Estado: " + s.estado
                            + " | Responsable: " + (s.resp == null ? "-" : s.resp.nombre);
                    if (s.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + s.obs;
                    }
                    System.out.println(linea);
                    cont++;
                }
            }
        } else if (op == 4) {
            System.out.print("Prioridad (1=Alta, 2=Media, 3=Baja): ");
            int p;
            try {
                p = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception ex) {
                p = -1;
            }
            for (Solicitud s : lista) {
                if (s.prioridad == p) {
                    String nom = "";
                    for (Estudiante e : estudiantes) {
                        if (e.legajo.equals(s.legajo)) {
                            nom = e.nombre;
                        }
                    }
                    String pr;
                    if (s.prioridad == 1) {
                        pr = "ALTA";
                    } else if (s.prioridad == 2) {
                        pr = "MEDIA";
                    } else {
                        pr = "BAJA";
                    }
                    String linea = "#" + s.id + " [" + s.tipo + "] " + s.desc + " | Estudiante: " + nom + " (" + s.legajo + ")"
                            + " | Prioridad: " + pr + " | Estado: " + s.estado
                            + " | Responsable: " + (s.resp == null ? "-" : s.resp.nombre);
                    if (s.estado.equals("RECHAZADA")) {
                        linea = linea + " | Motivo: " + s.obs;
                    }
                    System.out.println(linea);
                    cont++;
                }
            }
        } else {
            System.out.println("Opcion invalida.");
            return;
        }
        if (cont == 0) {
            System.out.println("No se encontraron solicitudes.");
        } else {
            System.out.println("Se encontraron " + cont + " solicitud(es).");
        }
    }

    public void listar() {
        if (lista.size() == 0) {
            System.out.println("No hay solicitudes registradas.");
            return;
        }
        System.out.println("Listado de solicitudes (" + lista.size() + "):");
        for (Solicitud s : lista) {
            String nom = "";
            for (Estudiante e : estudiantes) {
                if (e.legajo.equals(s.legajo)) {
                    nom = e.nombre;
                }
            }
            String pr;
            if (s.prioridad == 1) {
                pr = "ALTA";
            } else if (s.prioridad == 2) {
                pr = "MEDIA";
            } else {
                pr = "BAJA";
            }
            String linea = "#" + s.id + " [" + s.tipo + "] " + s.desc + " | Estudiante: " + nom + " (" + s.legajo + ")"
                    + " | Prioridad: " + pr + " | Estado: " + s.estado
                    + " | Responsable: " + (s.resp == null ? "-" : s.resp.nombre);
            if (s.estado.equals("RECHAZADA")) {
                linea = linea + " | Motivo: " + s.obs;
            }
            System.out.println(linea);
            //System.out.println(s.id + " - " + s.tipo + " - " + s.estado);
        }
    }

    public void reporte() {
        int total = lista.size();
        int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
        int t1 = 0, t2 = 0, t3 = 0, t4 = 0;
        int p1 = 0, p2 = 0, p3 = 0;
        int altaSinResolver = 0;
        int sinResp = 0;
        for (Solicitud s : lista) {
            if (s.estado.equals("PENDIENTE")) c1++;
            else if (s.estado.equals("EN_PROCESO")) c2++;
            else if (s.estado.equals("RESUELTA")) c3++;
            else if (s.estado.equals("RECHAZADA")) c4++;

            if (s.tipo.equals("CERTIFICADO")) t1++;
            else if (s.tipo.equals("EQUIVALENCIA")) t2++;
            else if (s.tipo.equals("CAMBIO_COMISION")) t3++;
            else if (s.tipo.equals("RECLAMO_NOTA")) t4++;

            if (s.prioridad == 1) p1++;
            else if (s.prioridad == 2) p2++;
            else p3++;

            if (s.prioridad == 1 && (s.estado.equals("PENDIENTE") || s.estado.equals("EN_PROCESO"))) {
                altaSinResolver++;
            }
            if (s.resp == null) {
                sinResp++;
            }
        }
        int porc = 0;
        if (total > 0) {
            porc = c3 * 100 / total;
        }
        System.out.println("===== REPORTE DE SOLICITUDES =====");
        System.out.println("Total de solicitudes: " + total);
        System.out.println("Por estado:");
        System.out.println("  PENDIENTE: " + c1);
        System.out.println("  EN_PROCESO: " + c2);
        System.out.println("  RESUELTA: " + c3);
        System.out.println("  RECHAZADA: " + c4);
        System.out.println("Por tipo:");
        System.out.println("  CERTIFICADO: " + t1);
        System.out.println("  EQUIVALENCIA: " + t2);
        System.out.println("  CAMBIO_COMISION: " + t3);
        System.out.println("  RECLAMO_NOTA: " + t4);
        System.out.println("Por prioridad:");
        System.out.println("  ALTA: " + p1);
        System.out.println("  MEDIA: " + p2);
        System.out.println("  BAJA: " + p3);
        System.out.println("Porcentaje de resueltas: " + porc + "%");
        System.out.println("Prioridad ALTA sin resolver: " + altaSinResolver);
        System.out.println("Sin responsable asignado: " + sinResp);
    }

    public Solicitud buscarPorId(int id) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).id == id) {
                return lista.get(i);
            }
        }
        return null;
    }
}
