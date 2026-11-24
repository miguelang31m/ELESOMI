package co.edu.aerolinea.pasajeros.servicio;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ErrorNegocio extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int estado;
    private final String codigo;

    public ErrorNegocio(int estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }
    public int getEstado() { return estado; }
    public String getCodigo() { return codigo; }
}
