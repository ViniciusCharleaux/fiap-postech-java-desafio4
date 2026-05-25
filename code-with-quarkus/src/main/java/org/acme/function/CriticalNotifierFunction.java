package org.acme.function;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.entity.Feedback;
import org.acme.repository.FeedbackRepository;
import org.acme.service.NotificationService;
import org.acme.enums.Urgencia;

import java.util.List;

@Path("/notificacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CriticalNotifierFunction {

    @Inject
    NotificationService notificationService;

    @Inject
    FeedbackRepository feedbackRepository;

    @POST
    @Path("/critica/{id}")
    public Response notificarFeedbackCritico(@PathParam("id") Long id) {
        Feedback feedback = feedbackRepository.findById(id);

        if (feedback == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Feedback não encontrado")
                    .build();
        }

        if (!Urgencia.CRITICA.equals(feedback.urgencia)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Feedback não é crítico")
                    .build();
        }

        notificationService.enviarNotificacaoCritica(feedback);
        return Response.ok("Notificação enviada com sucesso").build();
    }

    @GET
    @Path("/criticos")
    public Response listarCriticos() {
        List<Feedback> criticos = feedbackRepository.list("urgencia", Urgencia.CRITICA);
        return Response.ok(criticos).build();
    }
}