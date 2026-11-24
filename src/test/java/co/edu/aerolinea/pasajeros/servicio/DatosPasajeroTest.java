package co.edu.aerolinea.pasajeros.servicio;

import co.edu.aerolinea.pasajeros.modelo.Pasajero;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

public class DatosPasajeroTest {
    private JsonObjectBuilder valido() {
        return Json.createObjectBuilder()
            .add("nombres", "  Ana Maria  ")
            .add("apellidos", "Restrepo")
            .add("tipoDocumento", "cc")
            .add("numeroDocumento", " ab-1234 ")
            .add("fechaNacimiento", "1998-02-14")
            .add("correo", " ANA@EXAMPLE.COM ")
            .add("telefono", "3001234567")
            .add("nacionalidad", "Colombiana");
    }

    private ErrorNegocio error(JsonObject json) {
        try { new DatosPasajero(json); fail("Se esperaba un error de negocio"); }
        catch (ErrorNegocio e) { return e; }
        throw new AssertionError("No se lanzo ErrorNegocio");
    }

    private void comprobarError(JsonObject json, String codigo) {
        ErrorNegocio e = error(json);
        assertEquals(400, e.getEstado());
        assertEquals(codigo, e.getCodigo());
    }

    @Test public void normalizaDocumentoYCorreo() {
        DatosPasajero datos = new DatosPasajero(valido().build());
        Pasajero pasajero = new Pasajero();
        datos.aplicar(pasajero);
        assertEquals("CC", pasajero.getTipoDocumento());
        assertEquals("AB-1234", pasajero.getNumeroDocumento());
        assertEquals("Ana Maria", pasajero.getNombres());
        assertEquals("ana@example.com", pasajero.getCorreo());
        assertTrue(pasajero.isActivo());
    }

    @Test public void aceptaCamposOpcionalesOmitidos() {
        JsonObject json = Json.createObjectBuilder()
            .add("nombres", "Ana").add("apellidos", "Ruiz")
            .add("tipoDocumento", "PAS").add("numeroDocumento", "P12345")
            .add("fechaNacimiento", "2001-01-01").build();
        Pasajero p = new Pasajero();
        new DatosPasajero(json).aplicar(p);
        assertNull(p.getCorreo());
        assertNull(p.getTelefono());
        assertNull(p.getNacionalidad());
    }

    @Test public void rechazaCuerpoNulo() {
        comprobarError(null, "DATOS_INVALIDOS");
    }

    @Test public void rechazaNombreCorto() {
        comprobarError(valido().add("nombres", "A").build(), "CAMPO_INVALIDO");
    }

    @Test public void rechazaTipoDocumentoDesconocido() {
        comprobarError(valido().add("tipoDocumento", "DNI").build(), "TIPO_DOCUMENTO_INVALIDO");
    }

    @Test public void rechazaDocumentoConCaracteresInvalidos() {
        comprobarError(valido().add("numeroDocumento", "ABC/123").build(), "DOCUMENTO_INVALIDO");
    }

    @Test public void rechazaFechaImposible() {
        comprobarError(valido().add("fechaNacimiento", "2020-02-30").build(), "FECHA_INVALIDA");
    }

    @Test public void rechazaFechaFutura() {
        comprobarError(valido().add("fechaNacimiento", "2999-01-01").build(), "FECHA_INVALIDA");
    }

    @Test public void rechazaCorreoInvalido() {
        comprobarError(valido().add("correo", "sin-arroba").build(), "CORREO_INVALIDO");
    }

    @Test public void rechazaTelefonoInvalido() {
        comprobarError(valido().add("telefono", "abc1234").build(), "TELEFONO_INVALIDO");
    }

    @Test public void rechazaCampoConTipoNumerico() {
        comprobarError(valido().add("nombres", 15).build(), "DATOS_INVALIDOS");
    }

    @Test public void fechaNacimientoSeConserva() {
        Pasajero p = new Pasajero();
        new DatosPasajero(valido().build()).aplicar(p);
        assertEquals("1998-02-14", p.getFechaNacimiento().toString());
    }
}