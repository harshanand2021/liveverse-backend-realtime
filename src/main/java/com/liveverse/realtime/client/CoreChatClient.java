package com.liveverse.realtime.client;

import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.ChatMessageResponse;
import com.liveverse.realtime.client.dto.PostMessageRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "core-api")
@Path("/api/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CoreChatClient {

    @POST
    @Path("/{roomId}/messages")
    ApiResponseEnvelope<ChatMessageResponse> postMessage(@PathParam("roomId") Long roomId,
                                                         PostMessageRequest request);

    @GET
    @Path("/{roomId}/messages/recent")
    ApiResponseEnvelope<List<ChatMessageResponse>> getRecentMessages(@PathParam("roomId") Long roomId,
                                                                     @QueryParam("limit") int limit);
}