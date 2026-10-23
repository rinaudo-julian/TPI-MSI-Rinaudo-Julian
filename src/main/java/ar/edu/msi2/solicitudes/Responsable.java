package ar.edu.msi2.solicitudes;

public class Responsable {
    public int id;
    public String nombre;
    public Area area;

    public Responsable(String nombre, Area area) {
        this.nombre = nombre;
        this.area = area;
    }
}
