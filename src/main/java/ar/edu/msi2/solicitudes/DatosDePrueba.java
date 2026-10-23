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

        Responsable laura = repositorioResponsables.guardar(new Responsable("Laura Fernandez", Area.ALUMNADO));
        repositorioResponsables.guardar(new Responsable("Marcos Ruiz", Area.ALUMNADO));
        Responsable silvia = repositorioResponsables.guardar(new Responsable("Silvia Torres", Area.DEPARTAMENTO_ACADEMICO));

        repositorioSolicitudes.guardar(
                new Solicitud("1001", TipoSolicitud.CERTIFICADO, "Certificado de alumno regular", Prioridad.BAJA));

        Solicitud equivalencia = new Solicitud("1002", TipoSolicitud.EQUIVALENCIA, "Equivalencia de Algebra", Prioridad.MEDIA);
        equivalencia.asignarResponsable(silvia);
        equivalencia.cambiarEstado(EstadoSolicitud.EN_PROCESO);
        repositorioSolicitudes.guardar(equivalencia);

        repositorioSolicitudes.guardar(
                new Solicitud("1003", TipoSolicitud.RECLAMO_NOTA, "Revision de nota del parcial de Contabilidad", Prioridad.ALTA));

        Solicitud cambioComision = new Solicitud("1001", TipoSolicitud.CAMBIO_COMISION, "Cambio a comision noche", Prioridad.MEDIA);
        cambioComision.asignarResponsable(laura);
        cambioComision.cambiarEstado(EstadoSolicitud.EN_PROCESO);
        cambioComision.cambiarEstado(EstadoSolicitud.RESUELTA);
        repositorioSolicitudes.guardar(cambioComision);
    }
}
