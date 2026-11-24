package co.edu.aerolinea.pasajeros.modelo;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import javax.persistence.*;

@Entity
@Table(name = "pasajero", uniqueConstraints = {
    @UniqueConstraint(name = "uq_pasajero_documento", columnNames = {"tipo_documento", "numero_documento"})
})
public class Pasajero implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "tipo_documento", nullable = false, length = 3)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 25)
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private Date fechaNacimiento;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "telefono", length = 25)
    private String telefono;

    @Column(name = "nacionalidad", length = 60)
    private String nacionalidad;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private Timestamp creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Timestamp actualizadoEn;

    @PrePersist
    public void antesDeCrear() {
        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        creadoEn = ahora;
        actualizadoEn = ahora;
    }

    @PreUpdate
    public void antesDeActualizar() {
        actualizadoEn = new Timestamp(System.currentTimeMillis());
    }

    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
