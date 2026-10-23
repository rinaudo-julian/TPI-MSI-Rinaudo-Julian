package ar.edu.msi2.solicitudes.dominio;

public class Responsable {
    private int id;
    private final String nombre;
    private final Area area;

    public Responsable(String nombre, Area area) {
        this.nombre = nombre;
        this.area = area;
    }

    public void asignarId(int id) {
        if (this.id != 0) {
            throw new IllegalStateException("El responsable ya tiene id " + this.id);
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Area getArea() {
        return area;
    }
}
