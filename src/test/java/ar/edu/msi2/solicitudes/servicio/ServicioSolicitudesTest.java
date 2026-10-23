package ar.edu.msi2.solicitudes.servicio;

import ar.edu.msi2.solicitudes.dominio.Area;
import ar.edu.msi2.solicitudes.dominio.EstadoSolicitud;
import ar.edu.msi2.solicitudes.dominio.Estudiante;
import ar.edu.msi2.solicitudes.dominio.Prioridad;
import ar.edu.msi2.solicitudes.dominio.ReglaDeNegocioException;
import ar.edu.msi2.solicitudes.dominio.Responsable;
import ar.edu.msi2.solicitudes.dominio.Solicitud;
import ar.edu.msi2.solicitudes.dominio.TipoSolicitud;
import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsablesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudesEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests del servicio sin consola: se arma con repositorios en memoria y se verifica
 * lo que devuelve o lanza cada caso de uso.
 */
class ServicioSolicitudesTest {

    private RepositorioSolicitudesEnMemoria repositorioSolicitudes;
    private ServicioSolicitudes servicio;
    private Responsable laura;
    private Responsable silvia;

    @BeforeEach
    void armarServicio() {
        repositorioSolicitudes = new RepositorioSolicitudesEnMemoria();
        RepositorioEstudiantesEnMemoria repositorioEstudiantes = new RepositorioEstudiantesEnMemoria();
        RepositorioResponsablesEnMemoria repositorioResponsables = new RepositorioResponsablesEnMemoria();

        repositorioEstudiantes.guardar(new Estudiante("1001", "Ana Gomez", "ana.gomez@mail.com", "Ingenieria en Sistemas"));
        laura = repositorioResponsables.guardar(new Responsable("Laura Fernandez", Area.ALUMNADO));
        silvia = repositorioResponsables.guardar(new Responsable("Silvia Torres", Area.DEPARTAMENTO_ACADEMICO));

        servicio = new ServicioSolicitudes(repositorioSolicitudes, repositorioEstudiantes, repositorioResponsables);
    }

    @Test
    void registrarAsignaIdsConsecutivosYDejaLaSolicitudPendiente() {
        Solicitud primera = servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA);
        Solicitud segunda = servicio.registrarSolicitud("1001", TipoSolicitud.EQUIVALENCIA, "Equivalencia", Prioridad.MEDIA);

        assertEquals(1, primera.getId());
        assertEquals(2, segunda.getId());
        assertEquals(EstadoSolicitud.PENDIENTE, primera.getEstado());
        assertEquals(2, servicio.listarSolicitudes().size());
    }

    @Test
    void losReclamosDeNotaSiempreTienenPrioridadAlta() {
        Solicitud reclamo = servicio.registrarSolicitud("1001", TipoSolicitud.RECLAMO_NOTA, "Nota mal cargada", Prioridad.BAJA);

        assertEquals(Prioridad.ALTA, reclamo.getPrioridad());
    }

    @Test
    void noRegistraSolicitudesDeEstudiantesInexistentes() {
        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> servicio.registrarSolicitud("9999", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA));

        assertEquals("no existe un estudiante con legajo 9999", error.getMessage());
        assertEquals(0, servicio.listarSolicitudes().size());
    }

    @Test
    void asignaResponsableYCambiaEstadoHastaResolver() {
        int id = servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA).getId();

        servicio.asignarResponsable(id, laura.getId());
        servicio.cambiarEstado(id, EstadoSolicitud.EN_PROCESO);
        Solicitud resuelta = servicio.cambiarEstado(id, EstadoSolicitud.RESUELTA);

        assertEquals(EstadoSolicitud.RESUELTA, resuelta.getEstado());
        assertEquals("Laura Fernandez", resuelta.getResponsable().getNombre());
    }

    @Test
    void informaSolicitudesYResponsablesInexistentes() {
        int id = servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA).getId();

        assertEquals("no existe la solicitud 99",
                assertThrows(ReglaDeNegocioException.class, () -> servicio.cambiarEstado(99, EstadoSolicitud.EN_PROCESO)).getMessage());
        assertEquals("no existe el responsable 99",
                assertThrows(ReglaDeNegocioException.class, () -> servicio.asignarResponsable(id, 99)).getMessage());
    }

    @Test
    void rechazarGuardaElMotivo() {
        int id = servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA).getId();

        Solicitud rechazada = servicio.rechazar(id, "Duplicada");

        assertEquals(EstadoSolicitud.RECHAZADA, rechazada.getEstado());
        assertEquals("Duplicada", rechazada.getMotivoRechazo());
    }

    @Test
    void buscaSolicitudesSegunUnCriterio() {
        servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA);
        servicio.registrarSolicitud("1001", TipoSolicitud.EQUIVALENCIA, "Equivalencia", Prioridad.MEDIA);
        servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Analitico", Prioridad.MEDIA);

        List<Solicitud> certificados = servicio.buscarSolicitudes(s -> s.getTipo() == TipoSolicitud.CERTIFICADO);

        assertEquals(List.of(1, 3), certificados.stream().map(Solicitud::getId).toList());
    }

    @Test
    void elReporteIncluyeTodosLosValoresEnOrdenYRedondeaElPorcentajeHaciaAbajo() {
        int resuelta = servicio.registrarSolicitud("1001", TipoSolicitud.EQUIVALENCIA, "Equivalencia", Prioridad.MEDIA).getId();
        servicio.registrarSolicitud("1001", TipoSolicitud.RECLAMO_NOTA, "Reclamo", Prioridad.BAJA);
        servicio.registrarSolicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado", Prioridad.BAJA);
        servicio.asignarResponsable(resuelta, silvia.getId());
        servicio.cambiarEstado(resuelta, EstadoSolicitud.EN_PROCESO);
        servicio.cambiarEstado(resuelta, EstadoSolicitud.RESUELTA);

        ReporteSolicitudes reporte = servicio.generarReporte();

        assertEquals(3, reporte.total());
        assertEquals(List.of(EstadoSolicitud.values()), List.copyOf(reporte.porEstado().keySet()));
        assertEquals(List.of(2L, 0L, 1L, 0L), List.copyOf(reporte.porEstado().values()));
        assertEquals(List.of(1L, 1L, 0L, 1L), List.copyOf(reporte.porTipo().values()));
        assertEquals(List.of(1L, 1L, 1L), List.copyOf(reporte.porPrioridad().values()));
        assertEquals(33, reporte.porcentajeResueltas());
        assertEquals(1, reporte.altaSinResolver());
        assertEquals(2, reporte.sinResponsable());
    }

    @Test
    void elReporteSinSolicitudesNoDividePorCero() {
        ReporteSolicitudes reporte = servicio.generarReporte();

        assertEquals(0, reporte.total());
        assertEquals(0, reporte.porcentajeResueltas());
    }
}
