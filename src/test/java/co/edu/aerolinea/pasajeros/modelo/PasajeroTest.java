package co.edu.aerolinea.pasajeros.modelo;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasajeroTest {
    @Test public void unPasajeroNuevoEstaActivo() {
        assertTrue(new Pasajero().isActivo());
    }

    @Test public void permiteDesactivacionLogica() {
        Pasajero p = new Pasajero();
        p.setActivo(false);
        assertFalse(p.isActivo());
    }

    @Test public void persistenciaInicializaFechas() {
        Pasajero p = new Pasajero();
        p.antesDeCrear();
        try {
            Field creado = Pasajero.class.getDeclaredField("creadoEn");
            Field actualizado = Pasajero.class.getDeclaredField("actualizadoEn");
            creado.setAccessible(true); actualizado.setAccessible(true);
            assertNotNull(creado.get(p));
            assertNotNull(actualizado.get(p));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test public void propiedadesDelModeloConservanValores() {
        Pasajero p = new Pasajero();
        p.setNombres("Ana"); p.setApellidos("Ruiz");
        p.setTipoDocumento("CC"); p.setNumeroDocumento("12345");
        p.setCorreo("ana@example.com"); p.setTelefono("3001234567");
        p.setNacionalidad("Colombiana");
        assertEquals("Ana", p.getNombres());
        assertEquals("Ruiz", p.getApellidos());
        assertEquals("CC", p.getTipoDocumento());
        assertEquals("12345", p.getNumeroDocumento());
        assertEquals("ana@example.com", p.getCorreo());
        assertEquals("3001234567", p.getTelefono());
        assertEquals("Colombiana", p.getNacionalidad());
    }
}