package com.liveverse.realtime.client;

import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.PlaybackStateResponse;
import com.liveverse.realtime.client.dto.PlaybackStateUpdateRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "core-api")
@Path("/api/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CorePlaybackClient {

    @GET
    @Path("/{roomId}/playback")
    ApiResponseEnvelope<PlaybackStateResponse> getPlaybackState(@PathParam("roomId") Long roomId);

    @PUT
    @Path("/{roomId}/playback")
    ApiResponseEnvelope<PlaybackStateResponse> updatePlaybackState(@PathParam("roomId") Long roomId,
                                                                   PlaybackStateUpdateRequest request);
}
