package ar.edu.msi2.solicitudes.servicio;

import ar.edu.msi2.solicitudes.dominio.EstadoSolicitud;
import ar.edu.msi2.solicitudes.dominio.Prioridad;
import ar.edu.msi2.solicitudes.dominio.TipoSolicitud;

import java.util.Map;

/**
 * Resultado del reporte de solicitudes. Solo contiene datos: como se muestran es
 * responsabilidad de la capa de presentacion.
 */
public record ReporteSolicitudes(int total,
                                 Map<EstadoSolicitud, Long> porEstado,
                                 Map<TipoSolicitud, Long> porTipo,
                                 Map<Prioridad, Long> porPrioridad,
                                 long porcentajeResueltas,
                                 long altaSinResolver,
                                 long sinResponsable) {
}
