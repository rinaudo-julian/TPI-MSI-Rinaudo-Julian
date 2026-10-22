package ar.edu.msi2.solicitudes;

public class Main {
    public static void main(String[] args) {
        GestorSolicitudes g = new GestorSolicitudes();
        g.cargarDatos();
        g.menu();
    }
}
