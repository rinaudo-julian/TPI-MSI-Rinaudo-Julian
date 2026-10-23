package ar.edu.msi2.solicitudes.dominio;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoSolicitudTest {

    @ParameterizedTest(name = "{0} lo atiende {1}")
    @CsvSource({
            "CERTIFICADO,     ALUMNADO",
            "CAMBIO_COMISION, ALUMNADO",
            "EQUIVALENCIA,    DEPARTAMENTO_ACADEMICO",
            "RECLAMO_NOTA,    DEPARTAMENTO_ACADEMICO"
    })
    void cadaTipoTieneSuAreaResponsable(TipoSolicitud tipo, Area areaEsperada) {
        assertEquals(areaEsperada, tipo.getAreaResponsable());
    }
}
