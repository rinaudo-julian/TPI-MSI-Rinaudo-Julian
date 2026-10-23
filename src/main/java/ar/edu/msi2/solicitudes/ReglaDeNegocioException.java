package ar.edu.msi2.solicitudes;

/**
 * Se lanza cuando una operacion viola una regla de negocio del sistema.
 * El mensaje esta pensado para mostrarse al usuario.
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
