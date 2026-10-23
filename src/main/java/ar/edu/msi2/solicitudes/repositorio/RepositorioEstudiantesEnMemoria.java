package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.Estudiante;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioEstudiantesEnMemoria implements RepositorioEstudiantes {

    private final List<Estudiante> estudiantes = new ArrayList<>();

    @Override
    public Estudiante guardar(Estudiante estudiante) {
        for (int i = 0; i < estudiantes.size(); i++) {
            if (estudiantes.get(i).getLegajo().equals(estudiante.getLegajo())) {
                estudiantes.set(i, estudiante);
                return estudiante;
            }
        }
        estudiantes.add(estudiante);
        return estudiante;
    }

    @Override
    public Optional<Estudiante> buscarPorLegajo(String legajo) {
        return estudiantes.stream()
                .filter(estudiante -> estudiante.getLegajo().equals(legajo))
                .findFirst();
    }

    @Override
    public List<Estudiante> buscarTodos() {
        return List.copyOf(estudiantes);
    }

    @Override
    public void eliminar(String legajo) {
        estudiantes.removeIf(estudiante -> estudiante.getLegajo().equals(legajo));
    }
}
