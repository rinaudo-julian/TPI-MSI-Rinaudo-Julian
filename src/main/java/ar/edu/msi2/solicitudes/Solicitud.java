package ar.edu.msi2.solicitudes;

public class Solicitud {
    public int id;
    public String legajo;      // legajo del estudiante
    public String tipo;
    public String desc;
    public int prioridad;      // 1 = alta, 2 = media, 3 = baja
    public String estado;
    public Responsable resp;   // null si no tiene
    public String obs;         // motivo de rechazo
}
