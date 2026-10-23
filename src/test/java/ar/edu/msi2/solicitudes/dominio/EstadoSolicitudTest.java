package ar.edu.msi2.solicitudes.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoSolicitudTest {

    /**
     * Matriz completa de transiciones: 4 estados actuales x 4 estados nuevos.
     */
    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "PENDIENTE,  PENDIENTE,  false",
            "PENDIENTE,  EN_PROCESO, true",
            "PENDIENTE,  RESUELTA,   false",
            "PENDIENTE,  RECHAZADA,  true",
            "EN_PROCESO, PENDIENTE,  false",
            "EN_PROCESO, EN_PROCESO, false",
            "EN_PROCESO, RESUELTA,   true",
            "EN_PROCESO, RECHAZADA,  true",
            "RESUELTA,   PENDIENTE,  false",
            "RESUELTA,   EN_PROCESO, false",
            "RESUELTA,   RESUELTA,   false",
            "RESUELTA,   RECHAZADA,  false",
            "RECHAZADA,  PENDIENTE,  false",
            "RECHAZADA,  EN_PROCESO, false",
            "RECHAZADA,  RESUELTA,   false",
            "RECHAZADA,  RECHAZADA,  false"
    })
    void defineLasTransicionesPermitidas(EstadoSolicitud actual, EstadoSolicitud nuevo, boolean permitida) {
        assertEquals(permitida, actual.puedePasarA(nuevo));
    }

    @Test
    void soloResueltaYRechazadaEstanCerrados() {
        assertFalse(EstadoSolicitud.PENDIENTE.estaCerrado());
        assertFalse(EstadoSolicitud.EN_PROCESO.estaCerrado());
        assertTrue(EstadoSolicitud.RESUELTA.estaCerrado());
        assertTrue(EstadoSolicitud.RECHAZADA.estaCerrado());
    }
}
