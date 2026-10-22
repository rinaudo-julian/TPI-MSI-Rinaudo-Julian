package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.Responsable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioResponsablesEnMemoria implements RepositorioResponsables {

    private final List<Responsable> responsables = new ArrayList<>();
    private int ultimoId = 0;

    @Override
    public Responsable guardar(Responsable responsable) {
        if (responsable.id == 0) {
            ultimoId++;
            responsable.id = ultimoId;
            responsables.add(responsable);
            return responsable;
        }
        for (int i = 0; i < responsables.size(); i++) {
            if (responsables.get(i).id == responsable.id) {
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
                .filter(responsable -> responsable.id == id)
                .findFirst();
    }

    @Override
    public List<Responsable> buscarTodos() {
        return List.copyOf(responsables);
    }

    @Override
    public void eliminar(int id) {
        responsables.removeIf(responsable -> responsable.id == id);
    }
}
