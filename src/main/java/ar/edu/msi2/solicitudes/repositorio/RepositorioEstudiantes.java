package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.dominio.Estudiante;

import java.util.Optional;

public interface RepositorioEstudiantes {

    /**
     * Guarda el estudiante. Si ya existe uno con el mismo legajo, lo reemplaza.
     */
    Estudiante guardar(Estudiante estudiante);

    Optional<Estudiante> buscarPorLegajo(String legajo);
}
