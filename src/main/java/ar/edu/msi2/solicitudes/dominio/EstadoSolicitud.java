package ar.edu.msi2.solicitudes.dominio;

public enum EstadoSolicitud {
    PENDIENTE,
    EN_PROCESO,
    RESUELTA,
    RECHAZADA;

    public boolean puedePasarA(EstadoSolicitud nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == EN_PROCESO || nuevo == RECHAZADA;
            case EN_PROCESO -> nuevo == RESUELTA || nuevo == RECHAZADA;
            case RESUELTA, RECHAZADA -> false;
        };
    }

    public boolean estaCerrado() {
        return this == RESUELTA || this == RECHAZADA;
    }
}
