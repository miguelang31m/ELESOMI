package co.edu.aerolinea.pasajeros.api;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ErrorGeneralMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ErrorGeneralMapper.class.getName());
    @Override
    public Response toResponse(Exception ex) {
        if (ex instanceof WebApplicationException) {
            int estado = ((WebApplicationException) ex).getResponse().getStatus();
            return Response.status(estado).type(MediaType.APPLICATION_JSON)
                .entity(Json.createObjectBuilder().add("codigo", "ERROR_HTTP")
                    .add("mensaje", "Solicitud no disponible o invalida.").build().toString()).build();
        }
        LOG.log(Level.SEVERE, "Error no controlado en API", ex);
        return Response.status(500).type(MediaType.APPLICATION_JSON)
            .entity(Json.createObjectBuilder().add("codigo", "ERROR_INTERNO")
                .add("mensaje", "Error interno. Consulte los registros del servidor.").build().toString()).build();
    }
}
