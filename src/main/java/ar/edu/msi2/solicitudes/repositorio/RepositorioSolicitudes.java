package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.dominio.Solicitud;

import java.util.List;
import java.util.Optional;

public interface RepositorioSolicitudes {

    /**
     * Guarda la solicitud. Si es nueva (id == 0) le asigna el proximo id disponible;
     * si ya existe, la reemplaza.
     */
    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(int id);

    List<Solicitud> buscarTodas();

    void eliminar(int id);
}
