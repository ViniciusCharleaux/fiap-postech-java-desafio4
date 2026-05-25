package org.acme.function;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.dto.FeedbackRequest;
import org.acme.dto.FeedbackResponse;
import org.acme.service.FeedbackService;

@Path("/avaliacao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackReceiverFunction {

    @Inject
    FeedbackService feedbackService;

    @POST
    public Response receberAvaliacao(@Valid FeedbackRequest request) {
        FeedbackResponse response = feedbackService.receberFeedback(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("Feedback service is running").build();
    }
}