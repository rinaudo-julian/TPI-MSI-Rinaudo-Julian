package ar.edu.msi2.solicitudes;

import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantes;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsables;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudes;

public class DatosDePrueba {

    private final RepositorioSolicitudes repositorioSolicitudes;
    private final RepositorioEstudiantes repositorioEstudiantes;
    private final RepositorioResponsables repositorioResponsables;

    public DatosDePrueba(RepositorioSolicitudes repositorioSolicitudes,
                         RepositorioEstudiantes repositorioEstudiantes,
                         RepositorioResponsables repositorioResponsables) {
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioEstudiantes = repositorioEstudiantes;
        this.repositorioResponsables = repositorioResponsables;
    }

    public void cargar() {
        repositorioEstudiantes.guardar(new Estudiante("1001", "Ana Gomez", "ana.gomez@mail.com", "Ingenieria en Sistemas"));
        repositorioEstudiantes.guardar(new Estudiante("1002", "Bruno Diaz", "bruno.diaz@mail.com", "Ingenieria en Sistemas"));
        repositorioEstudiantes.guardar(new Estudiante("1003", "Carla Perez", "carla.perez@mail.com", "Licenciatura en Administracion"));

        Responsable laura = repositorioResponsables.guardar(new Responsable("Laura Fernandez", "ALUMNADO"));
        repositorioResponsables.guardar(new Responsable("Marcos Ruiz", "ALUMNADO"));
        Responsable silvia = repositorioResponsables.guardar(new Responsable("Silvia Torres", "DEPARTAMENTO ACADEMICO"));

        Solicitud certificado = new Solicitud();
        certificado.legajoEstudiante = "1001";
        certificado.tipo = "CERTIFICADO";
        certificado.descripcion = "Certificado de alumno regular";
        certificado.prioridad = 3;
        certificado.estado = "PENDIENTE";
        repositorioSolicitudes.guardar(certificado);

        Solicitud equivalencia = new Solicitud();
        equivalencia.legajoEstudiante = "1002";
        equivalencia.tipo = "EQUIVALENCIA";
        equivalencia.descripcion = "Equivalencia de Algebra";
        equivalencia.prioridad = 2;
        equivalencia.estado = "EN_PROCESO";
        equivalencia.responsable = silvia;
        repositorioSolicitudes.guardar(equivalencia);

        Solicitud reclamoNota = new Solicitud();
        reclamoNota.legajoEstudiante = "1003";
        reclamoNota.tipo = "RECLAMO_NOTA";
        reclamoNota.descripcion = "Revision de nota del parcial de Contabilidad";
        reclamoNota.prioridad = 1;
        reclamoNota.estado = "PENDIENTE";
        repositorioSolicitudes.guardar(reclamoNota);

        Solicitud cambioComision = new Solicitud();
        cambioComision.legajoEstudiante = "1001";
        cambioComision.tipo = "CAMBIO_COMISION";
        cambioComision.descripcion = "Cambio a comision noche";
        cambioComision.prioridad = 2;
        cambioComision.estado = "RESUELTA";
        cambioComision.responsable = laura;
        repositorioSolicitudes.guardar(cambioComision);
    }
}
