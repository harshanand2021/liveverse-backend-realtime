package com.liveverse.realtime.client;

import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.ReactionUpsertRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "core-api")
@Path("/api/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CoreReactionClient {

    @PUT
    @Path("/{messageId}/reactions")
    ApiResponseEnvelope<Void> upsertReaction(@PathParam("messageId") Long messageId,
                                             ReactionUpsertRequest request);
}