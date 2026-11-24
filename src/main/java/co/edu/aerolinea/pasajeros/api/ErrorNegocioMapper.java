package co.edu.aerolinea.pasajeros.api;

import co.edu.aerolinea.pasajeros.servicio.ErrorNegocio;
import javax.json.Json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ErrorNegocioMapper implements ExceptionMapper<ErrorNegocio> {
    @Override
    public Response toResponse(ErrorNegocio ex) {
        String cuerpo = Json.createObjectBuilder()
            .add("codigo", ex.getCodigo()).add("mensaje", ex.getMessage()).build().toString();
        return Response.status(ex.getEstado()).type(MediaType.APPLICATION_JSON).entity(cuerpo).build();
    }
}
