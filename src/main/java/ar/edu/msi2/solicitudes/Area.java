package ar.edu.msi2.solicitudes;

public enum Area {
    ALUMNADO("ALUMNADO"),
    DEPARTAMENTO_ACADEMICO("DEPARTAMENTO ACADEMICO");

    private final String nombre;

    Area(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
