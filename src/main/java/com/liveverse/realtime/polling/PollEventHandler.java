package com.liveverse.realtime.polling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.client.CorePollClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.CastVoteRequest;
import com.liveverse.realtime.client.dto.ClosePollRequest;
import com.liveverse.realtime.client.dto.CreatePollRequest;
import com.liveverse.realtime.client.dto.PollResponse;
import com.liveverse.realtime.exception.CoreServiceException;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.polling.dto.PollCloseRequest;
import com.liveverse.realtime.polling.dto.PollCreateRequest;
import com.liveverse.realtime.polling.dto.PollEvent;
import com.liveverse.realtime.polling.dto.PollOptionTally;
import com.liveverse.realtime.polling.dto.PollVoteRequest;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class PollEventHandler implements EventHandler {

    @Inject
    @RestClient
    CorePollClient corePollClient;

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.POLL_CREATE
                || type == EventType.POLL_VOTE
                || type == EventType.POLL_CLOSE;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        switch (event.type()) {
            case POLL_CREATE -> handleCreate(session, event);
            case POLL_VOTE -> handleVote(session, event);
            case POLL_CLOSE -> handleClose(session, event);
            default -> throw new IllegalStateException("Unsupported poll event: " + event.type());
        }
    }

    private void handleCreate(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can create a poll");
        }

        PollCreateRequest request = objectMapper.convertValue(event.payload(), PollCreateRequest.class);

        ApiResponseEnvelope<PollResponse> response;
        try {
            response = corePollClient.createPoll(
                    session.roomId(),
                    new CreatePollRequest(session.userId(), request.title(), request.options())
            );
        } catch (Exception e) {
            throw new CoreServiceException("Failed to create poll", e);
        }

        broadcast(session, EventType.POLL_CREATE, response);
    }

    private void handleVote(ParticipantSession session, InboundEvent event) {
        PollVoteRequest request = objectMapper.convertValue(event.payload(), PollVoteRequest.class);

        ApiResponseEnvelope<PollResponse> response;
        try {
            response = corePollClient.castVote(
                    request.pollId(),
                    new CastVoteRequest(session.userId(), request.optionId())
            );
        } catch (Exception e) {
            throw new CoreServiceException("Failed to cast vote", e);
        }

        broadcast(session, EventType.POLL_VOTE, response);
    }

    private void handleClose(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can close a poll");
        }

        PollCloseRequest request = objectMapper.convertValue(event.payload(), PollCloseRequest.class);

        ApiResponseEnvelope<PollResponse> response;
        try {
            response = corePollClient.closePoll(
                    request.pollId(),
                    new ClosePollRequest(session.userId())
            );
        } catch (Exception e) {
            throw new CoreServiceException("Failed to close poll", e);
        }

        broadcast(session, EventType.POLL_CLOSE, response);
    }

    private void broadcast(ParticipantSession session, EventType type, ApiResponseEnvelope<PollResponse> response) {
        if (!response.success() || response.data() == null) {
            throw new CoreServiceException(
                    response.message() != null ? response.message() : "Poll action rejected"
            );
        }

        sessionRegistry.broadcastToRoom(session.roomId(), new OutboundEvent(type, toPollEvent(response.data())));
    }

    private PollEvent toPollEvent(PollResponse poll) {
        List<PollOptionTally> tallies = poll.options().stream()
                .map(o -> new PollOptionTally(o.optionId(), o.optionTitle(), o.voteCount()))
                .toList();
        return new PollEvent(poll.pollId(), poll.title(), poll.status(), tallies);
    }
}
