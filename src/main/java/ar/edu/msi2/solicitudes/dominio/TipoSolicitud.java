package ar.edu.msi2.solicitudes.dominio;

public enum TipoSolicitud {
    CERTIFICADO(Area.ALUMNADO),
    EQUIVALENCIA(Area.DEPARTAMENTO_ACADEMICO),
    CAMBIO_COMISION(Area.ALUMNADO),
    RECLAMO_NOTA(Area.DEPARTAMENTO_ACADEMICO);

    private final Area areaResponsable;

    TipoSolicitud(Area areaResponsable) {
        this.areaResponsable = areaResponsable;
    }

    public Area getAreaResponsable() {
        return areaResponsable;
    }
}
