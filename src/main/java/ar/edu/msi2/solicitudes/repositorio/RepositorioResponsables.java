package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.Responsable;

import java.util.List;
import java.util.Optional;

public interface RepositorioResponsables {

    /**
     * Guarda el responsable. Si ya existe uno con el mismo id, lo reemplaza.
     */
    Responsable guardar(Responsable responsable);

    Optional<Responsable> buscarPorId(int id);

    List<Responsable> buscarTodos();

    void eliminar(int id);
}
