package co.edu.aerolinea.pasajeros.api;

import co.edu.aerolinea.pasajeros.modelo.Pasajero;
import co.edu.aerolinea.pasajeros.servicio.DatosPasajero;
import co.edu.aerolinea.pasajeros.servicio.ErrorNegocio;
import co.edu.aerolinea.pasajeros.servicio.PasajeroService;
import java.net.URI;
import java.util.List;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

@Path("/pasajeros")
@Produces(MediaType.APPLICATION_JSON)
public class PasajeroResource {
    @EJB
    private PasajeroService servicio;

    @GET
    public Response listar(@QueryParam("q") @DefaultValue("") String q,
                           @QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano) {
        if (pagina < 1 || tamano < 1 || tamano > 100)
            throw new ErrorNegocio(400, "PAGINACION_INVALIDA", "pagina >= 1 y tamano entre 1 y 100.");
        List<Pasajero> registros = servicio.listar(q, pagina, tamano);
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Pasajero p : registros) items.add(convertir(p));
        return Response.ok(Json.createObjectBuilder()
            .add("items", items)
            .add("pagina", pagina)
            .add("tamano", tamano)
            .add("total", servicio.contar(q))
            .build().toString()).build();
    }

    @GET
    @Path("/verificar")
    public Response verificar(@QueryParam("tipoDocumento") String tipo,
                             @QueryParam("numeroDocumento") String numero) {
        Pasajero p = servicio.verificar(tipo, numero);
        JsonObjectBuilder resultado = Json.createObjectBuilder().add("existe", p != null);
        if (p != null) resultado.add("id", p.getId());
        return Response.ok(resultado.build().toString()).build();
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") long id) {
        return Response.ok(convertir(servicio.buscar(id)).toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(JsonObject json, @Context UriInfo uriInfo) {
        Pasajero p = servicio.crear(new DatosPasajero(json));
        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(p.getId())).build();
        return Response.created(uri).entity(convertir(p).toString()).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") long id, JsonObject json) {
        return Response.ok(convertir(servicio.actualizar(id, new DatosPasajero(json))).toString()).build();
    }

    @DELETE
    @Path("/{id}")
    public Response desactivar(@PathParam("id") long id) {
        servicio.desactivar(id);
        return Response.noContent().build();
    }

    private JsonObject convertir(Pasajero p) {
        JsonObjectBuilder b = Json.createObjectBuilder()
            .add("id", p.getId())
            .add("nombres", p.getNombres())
            .add("apellidos", p.getApellidos())
            .add("tipoDocumento", p.getTipoDocumento())
            .add("numeroDocumento", p.getNumeroDocumento())
            .add("fechaNacimiento", p.getFechaNacimiento().toString())
            .add("activo", p.isActivo());
        b.add("correo", p.getCorreo() == null ? "" : p.getCorreo());
        b.add("telefono", p.getTelefono() == null ? "" : p.getTelefono());
        b.add("nacionalidad", p.getNacionalidad() == null ? "" : p.getNacionalidad());
        return b.build();
    }
}
