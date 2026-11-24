package co.edu.aerolinea.pasajeros.servicio;

import co.edu.aerolinea.pasajeros.modelo.Pasajero;
import java.util.List;
import java.util.Locale;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class PasajeroService {
    @PersistenceContext(unitName = "aerolineaPU")
    private EntityManager em;

    public List<Pasajero> listar(String busqueda, int pagina, int tamano) {
        String q = "%" + normalizar(busqueda) + "%";
        TypedQuery<Pasajero> consulta = em.createQuery(
            "SELECT p FROM Pasajero p WHERE p.activo = true AND (LOWER(p.nombres) LIKE :q " +
            "OR LOWER(p.apellidos) LIKE :q OR LOWER(p.numeroDocumento) LIKE :q) ORDER BY p.id DESC", Pasajero.class);
        return consulta.setParameter("q", q).setFirstResult((pagina - 1) * tamano)
            .setMaxResults(tamano).getResultList();
    }

    public long contar(String busqueda) {
        String q = "%" + normalizar(busqueda) + "%";
        return em.createQuery(
            "SELECT COUNT(p) FROM Pasajero p WHERE p.activo = true AND (LOWER(p.nombres) LIKE :q " +
            "OR LOWER(p.apellidos) LIKE :q OR LOWER(p.numeroDocumento) LIKE :q)", Long.class)
            .setParameter("q", q).getSingleResult();
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    public Pasajero buscar(Long id) {
        Pasajero p = em.find(Pasajero.class, id);
        if (p == null || !p.isActivo())
            throw new ErrorNegocio(404, "PASAJERO_NO_ENCONTRADO", "El pasajero no existe o esta inactivo.");
        return p;
    }

    public Pasajero verificar(String tipo, String numero) {
        if (tipo == null || numero == null || tipo.trim().isEmpty() || numero.trim().isEmpty())
            throw new ErrorNegocio(400, "DOCUMENTO_INVALIDO", "Debe indicar tipoDocumento y numeroDocumento.");
        List<Pasajero> encontrados = em.createQuery(
            "SELECT p FROM Pasajero p WHERE p.tipoDocumento = :tipo AND p.numeroDocumento = :numero AND p.activo = true", Pasajero.class)
            .setParameter("tipo", tipo.trim().toUpperCase(Locale.ROOT))
            .setParameter("numero", numero.trim().toUpperCase(Locale.ROOT))
            .setMaxResults(1).getResultList();
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private void comprobarDocumento(DatosPasajero datos, Long excluirId) {
        String jpql = "SELECT COUNT(p) FROM Pasajero p WHERE p.tipoDocumento = :tipo AND p.numeroDocumento = :numero";
        if (excluirId != null) jpql += " AND p.id <> :id";
        TypedQuery<Long> consulta = em.createQuery(jpql, Long.class)
            .setParameter("tipo", datos.getTipoDocumento())
            .setParameter("numero", datos.getNumeroDocumento());
        if (excluirId != null) consulta.setParameter("id", excluirId);
        if (consulta.getSingleResult() > 0)
            throw new ErrorNegocio(409, "DOCUMENTO_DUPLICADO", "Ya existe un pasajero con ese documento.");
    }

    public Pasajero crear(DatosPasajero datos) {
        comprobarDocumento(datos, null);
        Pasajero p = new Pasajero();
        datos.aplicar(p);
        em.persist(p);
        em.flush();
        return p;
    }

    public Pasajero actualizar(Long id, DatosPasajero datos) {
        Pasajero p = buscar(id);
        comprobarDocumento(datos, id);
        datos.aplicar(p);
        em.flush();
        return p;
    }

    public void desactivar(Long id) {
        Pasajero p = buscar(id);
        p.setActivo(false);
        em.flush();
    }
}
