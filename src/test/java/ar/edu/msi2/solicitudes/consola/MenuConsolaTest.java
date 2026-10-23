package ar.edu.msi2.solicitudes.consola;

import ar.edu.msi2.solicitudes.dominio.Area;
import ar.edu.msi2.solicitudes.dominio.Estudiante;
import ar.edu.msi2.solicitudes.dominio.Responsable;
import ar.edu.msi2.solicitudes.repositorio.RepositorioEstudiantesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioResponsablesEnMemoria;
import ar.edu.msi2.solicitudes.repositorio.RepositorioSolicitudesEnMemoria;
import ar.edu.msi2.solicitudes.servicio.ServicioSolicitudes;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Como MenuConsola recibe su entrada y su salida por constructor, se puede probar sin tocar
 * System.in ni System.out (algo imposible en la version inicial).
 */
class MenuConsolaTest {

    private String ejecutar(String entrada) {
        RepositorioEstudiantesEnMemoria estudiantes = new RepositorioEstudiantesEnMemoria();
        RepositorioResponsablesEnMemoria responsables = new RepositorioResponsablesEnMemoria();
        estudiantes.guardar(new Estudiante("1001", "Ana Gomez", "ana.gomez@mail.com", "Ingenieria en Sistemas"));
        responsables.guardar(new Responsable("Laura Fernandez", Area.ALUMNADO));
        ServicioSolicitudes servicio = new ServicioSolicitudes(new RepositorioSolicitudesEnMemoria(), estudiantes, responsables);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        new MenuConsola(servicio, new Scanner(entrada), new PrintStream(salida, true, StandardCharsets.UTF_8)).iniciar();
        return salida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void registraUnaSolicitudYLaMuestraEnElListado() {
        String salida = ejecutar("1\n1001\n1\nCertificado analitico\n2\n5\n0\n");

        assertTrue(salida.contains("Solicitud registrada con ID 1 para Ana Gomez."));
        assertTrue(salida.contains("#1 [CERTIFICADO] Certificado analitico | Estudiante: Ana Gomez (1001) | Prioridad: MEDIA"));
    }

    @Test
    void muestraLosErroresDeNegocioConElPrefijoError() {
        String salida = ejecutar("2\n99\n0\n");

        assertTrue(salida.contains("Error: no existe la solicitud 99"));
    }

    @Test
    void conUnaListaVaciaLoInforma() {
        String salida = ejecutar("5\n0\n");

        assertTrue(salida.contains("No hay solicitudes registradas."));
    }
}
