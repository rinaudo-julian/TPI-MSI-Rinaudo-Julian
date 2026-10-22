package ar.edu.msi2.solicitudes.repositorio;

import ar.edu.msi2.solicitudes.Solicitud;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioSolicitudesEnMemoria implements RepositorioSolicitudes {

    private final List<Solicitud> solicitudes = new ArrayList<>();
    private int ultimoId = 0;

    @Override
    public Solicitud guardar(Solicitud solicitud) {
        if (solicitud.id == 0) {
            ultimoId++;
            solicitud.id = ultimoId;
            solicitudes.add(solicitud);
            return solicitud;
        }
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).id == solicitud.id) {
                solicitudes.set(i, solicitud);
                return solicitud;
            }
        }
        solicitudes.add(solicitud);
        return solicitud;
    }

    @Override
    public Optional<Solicitud> buscarPorId(int id) {
        return solicitudes.stream()
                .filter(solicitud -> solicitud.id == id)
                .findFirst();
    }

    @Override
    public List<Solicitud> buscarTodas() {
        return List.copyOf(solicitudes);
    }

    @Override
    public void eliminar(int id) {
        solicitudes.removeIf(solicitud -> solicitud.id == id);
    }
}
