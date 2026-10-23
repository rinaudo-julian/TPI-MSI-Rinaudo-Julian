package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.dominio.Prioridad;
import ar.edu.msi2.solicitudes.dominio.Solicitud;
import ar.edu.msi2.solicitudes.dominio.TipoSolicitud;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioSolicitudesEnMemoriaTest {

    private final RepositorioSolicitudesEnMemoria repositorio = new RepositorioSolicitudesEnMemoria();

    private Solicitud nueva(String descripcion) {
        return new Solicitud("1001", TipoSolicitud.CERTIFICADO, descripcion, Prioridad.BAJA);
    }

    @Test
    void asignaIdsConsecutivosALasSolicitudesNuevas() {
        assertEquals(1, repositorio.guardar(nueva("a")).getId());
        assertEquals(2, repositorio.guardar(nueva("b")).getId());
    }

    @Test
    void guardarUnaExistenteLaReemplazaSinCambiarSuPosicion() {
        Solicitud primera = repositorio.guardar(nueva("a"));
        repositorio.guardar(nueva("b"));

        repositorio.guardar(primera);

        List<Solicitud> todas = repositorio.buscarTodas();
        assertEquals(2, todas.size());
        assertSame(primera, todas.get(0));
    }

    @Test
    void buscarPorIdDevuelveVacioSiNoExiste() {
        repositorio.guardar(nueva("a"));

        assertTrue(repositorio.buscarPorId(1).isPresent());
        assertTrue(repositorio.buscarPorId(99).isEmpty());
    }

    @Test
    void laListaDevueltaNoPermiteModificarElRepositorio() {
        repositorio.guardar(nueva("a"));
        List<Solicitud> todas = repositorio.buscarTodas();

        assertThrows(UnsupportedOperationException.class, () -> todas.add(nueva("b")));
        assertEquals(1, repositorio.buscarTodas().size());
    }
}
