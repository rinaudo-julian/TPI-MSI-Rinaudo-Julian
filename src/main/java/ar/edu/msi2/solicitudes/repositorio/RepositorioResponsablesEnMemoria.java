package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.dominio.Responsable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioResponsablesEnMemoria implements RepositorioResponsables {

    private final List<Responsable> responsables = new ArrayList<>();
    private int ultimoId = 0;

    @Override
    public Responsable guardar(Responsable responsable) {
        if (responsable.getId() == 0) {
            ultimoId++;
            responsable.asignarId(ultimoId);
            responsables.add(responsable);
            return responsable;
        }
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).getId() == responsable.getId()) {
                responsables.set(i, responsable);
                return responsable;
            }
        }
        responsables.add(responsable);
        return responsable;
    }

    @Override
    public Optional<Responsable> buscarPorId(int id) {
        return responsables.stream()
                .filter(responsable -> responsable.getId() == id)
                .findFirst();
    }

    @Override
    public List<Responsable> buscarTodos() {
        return List.copyOf(responsables);
    }
}
