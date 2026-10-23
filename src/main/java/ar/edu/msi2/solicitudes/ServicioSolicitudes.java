package ar.edu.msi2.solicitudes;

import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantes;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsables;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudes;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Casos de uso del sistema. No lee ni escribe en consola: recibe datos, aplica las reglas
 * de negocio y devuelve resultados o lanza {@link ReglaDeNegocioException}.
 */
public class ServicioSolicitudes {

    private final RepositorioSolicitudes repositorioSolicitudes;
    private final RepositorioEstudiantes repositorioEstudiantes;
    private final RepositorioResponsables repositorioResponsables;

    public ServicioSolicitudes(RepositorioSolicitudes repositorioSolicitudes,
                               RepositorioEstudiantes repositorioEstudiantes,
                               RepositorioResponsables repositorioResponsables) {
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioEstudiantes = repositorioEstudiantes;
        this.repositorioResponsables = repositorioResponsables;
    }

    public Solicitud registrarSolicitud(String legajo, TipoSolicitud tipo, String descripcion, Prioridad prioridadIngresada) {
        buscarEstudiante(legajo);
        Solicitud solicitud = new Solicitud(legajo, tipo, descripcion, prioridadSegunPolitica(tipo, prioridadIngresada));
        return repositorioSolicitudes.guardar(solicitud);
    }

    /**
     * Los reclamos de nota siempre van con prioridad alta (pedido de secretaria).
     */
    private Prioridad prioridadSegunPolitica(TipoSolicitud tipo, Prioridad prioridadIngresada) {
        if (tipo == TipoSolicitud.RECLAMO_NOTA) {
            return Prioridad.ALTA;
        }
        return prioridadIngresada;
    }

    public Solicitud cambiarEstado(int idSolicitud, EstadoSolicitud estadoNuevo) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        solicitud.cambiarEstado(estadoNuevo);
        return repositorioSolicitudes.guardar(solicitud);
    }

    public Solicitud rechazar(int idSolicitud, String motivo) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        solicitud.rechazar(motivo);
        return repositorioSolicitudes.guardar(solicitud);
    }

    public Solicitud asignarResponsable(int idSolicitud, int idResponsable) {
        Solicitud solicitud = buscarSolicitud(idSolicitud);
        Responsable responsable = buscarResponsable(idResponsable);
        solicitud.asignarResponsable(responsable);
        return repositorioSolicitudes.guardar(solicitud);
    }

    public Estudiante buscarEstudiante(String legajo) {
        return repositorioEstudiantes.buscarPorLegajo(legajo)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe un estudiante con legajo " + legajo));
    }

    public Optional<Estudiante> estudianteDe(Solicitud solicitud) {
        return repositorioEstudiantes.buscarPorLegajo(solicitud.getLegajoEstudiante());
    }

    public Solicitud buscarSolicitud(int id) {
        return repositorioSolicitudes.buscarPorId(id)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe la solicitud " + id));
    }

    public Responsable buscarResponsable(int id) {
        return repositorioResponsables.buscarPorId(id)
                .orElseThrow(() -> new ReglaDeNegocioException("no existe el responsable " + id));
    }

    public List<Responsable> listarResponsables() {
        return repositorioResponsables.buscarTodos();
    }

    public List<Solicitud> listarSolicitudes() {
        return repositorioSolicitudes.buscarTodas();
    }

    public List<Solicitud> buscarSolicitudes(Predicate<Solicitud> criterio) {
        return repositorioSolicitudes.buscarTodas().stream()
                .filter(criterio)
                .toList();
    }

    public ReporteSolicitudes generarReporte() {
        List<Solicitud> solicitudes = repositorioSolicitudes.buscarTodas();
        Map<EstadoSolicitud, Long> porEstado = contarPor(solicitudes, EstadoSolicitud.class, Solicitud::getEstado);
        return new ReporteSolicitudes(
                solicitudes.size(),
                porEstado,
                contarPor(solicitudes, TipoSolicitud.class, Solicitud::getTipo),
                contarPor(solicitudes, Prioridad.class, Solicitud::getPrioridad),
                porcentaje(porEstado.get(EstadoSolicitud.RESUELTA), solicitudes.size()),
                solicitudes.stream().filter(Solicitud::esAltaSinResolver).count(),
                solicitudes.stream().filter(solicitud -> !solicitud.tieneResponsable()).count());
    }

    /**
     * Cuenta las solicitudes por cada valor del enum, incluyendo los valores sin solicitudes (con 0).
     * El EnumMap mantiene el orden de declaracion del enum.
     */
    private static <E extends Enum<E>> Map<E, Long> contarPor(List<Solicitud> solicitudes, Class<E> tipoEnum,
                                                            Function<Solicitud, E> clasificador) {
        Map<E, Long> conteo = new EnumMap<>(tipoEnum);
        for (E valor : tipoEnum.getEnumConstants()) {
            conteo.put(valor, 0L);
        }
        for (Solicitud solicitud : solicitudes) {
            conteo.merge(clasificador.apply(solicitud), 1L, Long::sum);
        }
        return conteo;
    }

    private static long porcentaje(long parte, int total) {
        if (total == 0) {
            return 0;
        }
        return parte * 100 / total;
    }
}
