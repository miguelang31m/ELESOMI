package co.edu.aerolinea.pasajeros.servicio;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import co.edu.aerolinea.pasajeros.modelo.Pasajero;

public class DatosPasajero {
    private static final Set<String> TIPOS = new HashSet<>(Arrays.asList("CC", "CE", "PAS", "TI"));
    private final String nombres;
    private final String apellidos;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final Date fechaNacimiento;
    private final String correo;
    private final String telefono;
    private final String nacionalidad;

    public DatosPasajero(JsonObject json) {
        if (json == null) throw new ErrorNegocio(400, "DATOS_INVALIDOS", "Debe enviar un objeto JSON.");
        nombres = obligatorio(json, "nombres", 2, 80);
        apellidos = obligatorio(json, "apellidos", 2, 80);
        tipoDocumento = obligatorio(json, "tipoDocumento", 2, 3).toUpperCase(Locale.ROOT);
        numeroDocumento = obligatorio(json, "numeroDocumento", 4, 25).toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipoDocumento))
            throw new ErrorNegocio(400, "TIPO_DOCUMENTO_INVALIDO", "Use CC, CE, PAS o TI.");
        if (!numeroDocumento.matches("[A-Z0-9-]{4,25}"))
            throw new ErrorNegocio(400, "DOCUMENTO_INVALIDO", "El documento solo admite letras, numeros y guiones.");
        String fecha = obligatorio(json, "fechaNacimiento", 10, 10);
        try {
            LocalDate dia = LocalDate.parse(fecha);
            if (dia.isAfter(LocalDate.now()))
                throw new ErrorNegocio(400, "FECHA_INVALIDA", "La fecha de nacimiento no puede ser futura.");
            fechaNacimiento = Date.valueOf(dia);
        } catch (DateTimeParseException ex) {
            throw new ErrorNegocio(400, "FECHA_INVALIDA", "Use fechaNacimiento en formato AAAA-MM-DD.");
        }
        correo = opcional(json, "correo", 120).toLowerCase(Locale.ROOT);
        telefono = opcional(json, "telefono", 25);
        nacionalidad = opcional(json, "nacionalidad", 60);
        if (!correo.isEmpty() && !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new ErrorNegocio(400, "CORREO_INVALIDO", "El correo electronico no es valido.");
        if (!telefono.isEmpty() && !telefono.matches("[0-9+() .-]{7,25}"))
            throw new ErrorNegocio(400, "TELEFONO_INVALIDO", "El telefono no es valido.");
    }

    private static String leer(JsonObject json, String clave) {
        JsonValue valor = json.get(clave);
        if (valor == null || valor == JsonValue.NULL) return "";
        if (!(valor instanceof JsonString))
            throw new ErrorNegocio(400, "DATOS_INVALIDOS", "El campo " + clave + " debe ser texto.");
        return ((JsonString) valor).getString().trim();
    }
    private static String obligatorio(JsonObject json, String clave, int minimo, int maximo) {
        String texto = leer(json, clave);
        if (texto.length() < minimo || texto.length() > maximo)
            throw new ErrorNegocio(400, "CAMPO_INVALIDO", "El campo " + clave + " debe tener entre " + minimo + " y " + maximo + " caracteres.");
        return texto;
    }
    private static String opcional(JsonObject json, String clave, int maximo) {
        String texto = leer(json, clave);
        if (texto.length() > maximo)
            throw new ErrorNegocio(400, "CAMPO_INVALIDO", "El campo " + clave + " supera " + maximo + " caracteres.");
        return texto;
    }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void aplicar(Pasajero p) {
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTipoDocumento(tipoDocumento);
        p.setNumeroDocumento(numeroDocumento);
        p.setFechaNacimiento(fechaNacimiento);
        p.setCorreo(correo.isEmpty() ? null : correo);
        p.setTelefono(telefono.isEmpty() ? null : telefono);
        p.setNacionalidad(nacionalidad.isEmpty() ? null : nacionalidad);
    }
}
