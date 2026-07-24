package com.liveverse.realtime.client;

import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.RoomDetailsResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "core-api")
@Path("/api/rooms")
@Produces(MediaType.APPLICATION_JSON)
public interface CoreRoomClient {

    @GET
    @Path("/{roomId}")
    ApiResponseEnvelope<RoomDetailsResponse> getRoomDetails(@PathParam("roomId") Long roomId);
}