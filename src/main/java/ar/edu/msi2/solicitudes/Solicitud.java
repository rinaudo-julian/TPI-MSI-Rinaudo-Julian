package ar.edu.msi2.solicitudes;

public class Solicitud {

    private static final int LONGITUD_MAXIMA_DESCRIPCION = 200;

    private int id;
    private final String legajoEstudiante;
    private final TipoSolicitud tipo;
    private final String descripcion;
    private final Prioridad prioridad;
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;
    private Responsable responsable;
    private String motivoRechazo;

    public Solicitud(String legajoEstudiante, TipoSolicitud tipo, String descripcion, Prioridad prioridad) {
        validarDescripcion(descripcion);
        this.legajoEstudiante = legajoEstudiante;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
    }

    public static void validarDescripcion(String descripcion) {
        if (descripcion.isEmpty()) {
            throw new ReglaDeNegocioException("la descripcion no puede estar vacia");
        }
        if (descripcion.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            throw new ReglaDeNegocioException("la descripcion no puede superar los " + LONGITUD_MAXIMA_DESCRIPCION + " caracteres");
        }
    }

    public void asignarId(int id) {
        if (this.id != 0) {
            throw new IllegalStateException("La solicitud ya tiene id " + this.id);
        }
        this.id = id;
    }

    public void verificarQueAdmiteResponsable() {
        if (estaCerrada()) {
            throw new ReglaDeNegocioException("no se puede asignar responsable a una solicitud cerrada");
        }
    }

    public void asignarResponsable(Responsable responsable) {
        verificarQueAdmiteResponsable();
        Area areaNecesaria = tipo.getAreaResponsable();
        if (responsable.getArea() != areaNecesaria) {
            throw new ReglaDeNegocioException("las solicitudes de tipo " + tipo + " las atiende el area " + areaNecesaria.getNombre());
        }
        this.responsable = responsable;
    }

    public void verificarTransicionA(EstadoSolicitud nuevo) {
        if (!estado.puedePasarA(nuevo)) {
            throw new ReglaDeNegocioException("no se puede pasar de " + estado + " a " + nuevo);
        }
        if (nuevo == EstadoSolicitud.EN_PROCESO && !tieneResponsable()) {
            throw new ReglaDeNegocioException("la solicitud debe tener un responsable asignado para pasar a EN_PROCESO");
        }
    }

    public void cambiarEstado(EstadoSolicitud nuevo) {
        if (nuevo == EstadoSolicitud.RECHAZADA) {
            throw new IllegalArgumentException("Para rechazar una solicitud usar rechazar(motivo)");
        }
        verificarTransicionA(nuevo);
        this.estado = nuevo;
    }

    public void rechazar(String motivo) {
        verificarTransicionA(EstadoSolicitud.RECHAZADA);
        if (motivo.isEmpty()) {
            throw new ReglaDeNegocioException("el motivo del rechazo es obligatorio");
        }
        this.motivoRechazo = motivo;
        this.estado = EstadoSolicitud.RECHAZADA;
    }

    public boolean estaCerrada() {
        return estado.estaCerrado();
    }

    public boolean tieneResponsable() {
        return responsable != null;
    }

    public boolean esAltaSinResolver() {
        return prioridad == Prioridad.ALTA && !estaCerrada();
    }

    public int getId() {
        return id;
    }

    public String getLegajoEstudiante() {
        return legajoEstudiante;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public Responsable getResponsable() {
        return responsable;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }
}
