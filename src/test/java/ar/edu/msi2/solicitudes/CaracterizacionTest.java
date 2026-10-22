package ar.edu.msi2.solicitudes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests de caracterizacion (golden master).
 *
 * Fijan el comportamiento externo observable de la version inicial del sistema: para cada
 * escenario se ejecuta la aplicacion completa a traves de {@link Main#main(String[])} con la
 * entrada de {@code caracterizacion/<escenario>.in} y se compara la salida de consola contra
 * {@code caracterizacion/<escenario>.out}, grabada a partir de la version inicial.
 *
 * Los archivos .out NO deben modificarse durante el refactoring: si un test falla, el
 * refactoring cambio el comportamiento externo.
 */
class CaracterizacionTest {

    private static final String CARPETA = "caracterizacion";

    private InputStream entradaOriginal;
    private PrintStream salidaOriginal;

    @BeforeEach
    void guardarConsola() {
        entradaOriginal = System.in;
        salidaOriginal = System.out;
    }

    @AfterEach
    void restaurarConsola() {
        System.setIn(entradaOriginal);
        System.setOut(salidaOriginal);
    }

    static Stream<String> escenarios() throws IOException, URISyntaxException {
        URL carpeta = CaracterizacionTest.class.getClassLoader().getResource(CARPETA);
        try (Stream<Path> archivos = Files.list(Path.of(carpeta.toURI()))) {
            return archivos
                    .map(archivo -> archivo.getFileName().toString())
                    .filter(nombre -> nombre.endsWith(".in"))
                    .map(nombre -> nombre.substring(0, nombre.length() - ".in".length()))
                    .sorted()
                    .toList()
                    .stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("escenarios")
    void laSalidaCoincideConLaVersionInicial(String escenario) throws IOException {
        String entrada = leerRecurso(escenario + ".in");
        String salidaEsperada = leerRecurso(escenario + ".out");

        String salidaObtenida = ejecutarAplicacion(entrada);

        assertEquals(normalizar(salidaEsperada), normalizar(salidaObtenida));
    }

    private String ejecutarAplicacion(String entrada) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(salida, true, StandardCharsets.UTF_8));

        Main.main(new String[0]);

        System.out.flush();
        return salida.toString(StandardCharsets.UTF_8);
    }

    private String leerRecurso(String nombre) throws IOException {
        try (InputStream recurso = getClass().getClassLoader().getResourceAsStream(CARPETA + "/" + nombre)) {
            if (recurso == null) {
                throw new IOException("No se encontro el recurso " + CARPETA + "/" + nombre);
            }
            return new String(recurso.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String normalizar(String texto) {
        return texto.replace("\r\n", "\n");
    }
}
