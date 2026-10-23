package ar.edu.msi2.solicitudes.dominio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitudTest {

    private final Responsable deAlumnado = new Responsable("Laura Fernandez", Area.ALUMNADO);
    private final Responsable deDepartamento = new Responsable("Silvia Torres", Area.DEPARTAMENTO_ACADEMICO);

    private Solicitud certificado() {
        return new Solicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado de alumno regular", Prioridad.BAJA);
    }

    @Test
    void unaSolicitudNuevaEstaPendienteYSinResponsable() {
        Solicitud solicitud = certificado();

        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertFalse(solicitud.tieneResponsable());
        assertNull(solicitud.getMotivoRechazo());
    }

    @Test
    void laDescripcionNoPuedeEstarVacia() {
        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> new Solicitud("1001", TipoSolicitud.CERTIFICADO, "", Prioridad.BAJA));

        assertEquals("la descripcion no puede estar vacia", error.getMessage());
    }

    @Test
    void laDescripcionAdmiteHasta200Caracteres() {
        assertDoesNotThrow(() -> Solicitud.validarDescripcion("x".repeat(200)));

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> Solicitud.validarDescripcion("x".repeat(201)));
        assertEquals("la descripcion no puede superar los 200 caracteres", error.getMessage());
    }

    @Test
    void elIdSoloPuedeAsignarseUnaVez() {
        Solicitud solicitud = certificado();
        solicitud.asignarId(7);

        assertEquals(7, solicitud.getId());
        assertThrows(IllegalStateException.class, () -> solicitud.asignarId(8));
    }

    @Test
    void aceptaUnResponsableDelAreaQueAtiendeSuTipo() {
        Solicitud solicitud = certificado();

        solicitud.asignarResponsable(deAlumnado);

        assertTrue(solicitud.tieneResponsable());
        assertEquals(deAlumnado, solicitud.getResponsable());
    }

    @Test
    void rechazaUnResponsableDeOtraArea() {
        Solicitud solicitud = certificado();

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> solicitud.asignarResponsable(deDepartamento));

        assertEquals("las solicitudes de tipo CERTIFICADO las atiende el area ALUMNADO", error.getMessage());
        assertFalse(solicitud.tieneResponsable());
    }

    @Test
    void noAdmiteResponsableSiEstaCerrada() {
        Solicitud solicitud = certificado();
        solicitud.rechazar("Duplicada");

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> solicitud.asignarResponsable(deAlumnado));

        assertEquals("no se puede asignar responsable a una solicitud cerrada", error.getMessage());
    }

    @Test
    void necesitaResponsableParaPasarAEnProceso() {
        Solicitud solicitud = certificado();

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO));

        assertEquals("la solicitud debe tener un responsable asignado para pasar a EN_PROCESO", error.getMessage());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
    }

    @Test
    void recorreElCicloCompletoHastaResuelta() {
        Solicitud solicitud = certificado();
        solicitud.asignarResponsable(deAlumnado);

        solicitud.cambiarEstado(EstadoSolicitud.EN_PROCESO);
        solicitud.cambiarEstado(EstadoSolicitud.RESUELTA);

        assertEquals(EstadoSolicitud.RESUELTA, solicitud.getEstado());
        assertTrue(solicitud.estaCerrada());
    }

    @Test
    void noPermiteTransicionesInvalidas() {
        Solicitud solicitud = certificado();

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class,
                () -> solicitud.cambiarEstado(EstadoSolicitud.RESUELTA));

        assertEquals("no se puede pasar de PENDIENTE a RESUELTA", error.getMessage());
    }

    @Test
    void rechazarExigeMotivoYLoGuarda() {
        Solicitud solicitud = certificado();

        ReglaDeNegocioException error = assertThrows(ReglaDeNegocioException.class, () -> solicitud.rechazar(""));
        assertEquals("el motivo del rechazo es obligatorio", error.getMessage());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());

        solicitud.rechazar("Fuera de termino");
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertEquals("Fuera de termino", solicitud.getMotivoRechazo());
    }

    @Test
    void paraRechazarSeDebeUsarRechazar() {
        Solicitud solicitud = certificado();

        assertThrows(IllegalArgumentException.class, () -> solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA));
    }

    @Test
    void esAltaSinResolverSoloSiEsAltaYEstaAbierta() {
        Solicitud alta = new Solicitud("1003", TipoSolicitud.RECLAMO_NOTA, "Revision de nota", Prioridad.ALTA);
        assertTrue(alta.esAltaSinResolver());

        alta.rechazar("Fuera de termino");
        assertFalse(alta.esAltaSinResolver());

        assertFalse(certificado().esAltaSinResolver());
    }
}
