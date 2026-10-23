package ar.edu.msi2.solicitudes;

import ar.edu.msi2.solicitudes.consola.MenuConsola;
import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantes;
import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsables;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsablesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudes;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudesEnMemoria;
import ar.edu.msi2.solicitudes.servicio.ServicioSolicitudes;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RepositorioSolicitudes repositorioSolicitudes = new RepositorioSolicitudesEnMemoria();
        RepositorioEstudiantes repositorioEstudiantes = new RepositorioEstudiantesEnMemoria();
        RepositorioResponsables repositorioResponsables = new RepositorioResponsablesEnMemoria();

        new DatosDePrueba(repositorioSolicitudes, repositorioEstudiantes, repositorioResponsables).cargar();

        ServicioSolicitudes servicio = new ServicioSolicitudes(repositorioSolicitudes, repositorioEstudiantes, repositorioResponsables);
        MenuConsola menu = new MenuConsola(servicio, new Scanner(System.in), System.out);
        menu.iniciar();
    }
}
