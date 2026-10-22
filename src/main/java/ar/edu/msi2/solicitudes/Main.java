package ar.edu.msi2.solicitudes;

public class Main {
    public static void main(String[] args) {
        GestorSolicitudes gestor = new GestorSolicitudes();
        gestor.cargarDatos();
        gestor.mostrarMenu();
    }
}
