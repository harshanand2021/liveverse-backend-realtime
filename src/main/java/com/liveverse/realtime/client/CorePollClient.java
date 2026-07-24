package com.liveverse.realtime.client;

import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.CastVoteRequest;
import com.liveverse.realtime.client.dto.ClosePollRequest;
import com.liveverse.realtime.client.dto.CreatePollRequest;
import com.liveverse.realtime.client.dto.PollResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "core-api")
@Path("/api/rooms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CorePollClient {

    @POST
    @Path("/{roomId}/polls")
    ApiResponseEnvelope<PollResponse> createPoll(@PathParam("roomId") Long roomId,
                                                 CreatePollRequest request);

    @POST
    @Path("/polls/{pollId}/votes")
    ApiResponseEnvelope<PollResponse> castVote(@PathParam("pollId") Long pollId,
                                               CastVoteRequest request);

    @POST
    @Path("/polls/{pollId}/close")
    ApiResponseEnvelope<PollResponse> closePoll(@PathParam("pollId") Long pollId,
                                                ClosePollRequest request);

    @GET
    @Path("/{roomId}/polls/active")
    ApiResponseEnvelope<PollResponse> getActivePoll(@PathParam("roomId") Long roomId);
}