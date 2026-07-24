package com.liveverse.realtime.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.sync.dto.PlaybackStateEvent;
import com.liveverse.realtime.sync.dto.PlaybackStateRequest;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class PlaybackStateEventHandler implements EventHandler {

    @Inject
    PlaybackStateCache playbackStateCache;

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.PLAYBACK_STATE_HINT;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can change playback state");
        }

        PlaybackStateRequest request = objectMapper.convertValue(event.payload(), PlaybackStateRequest.class);

        PlaybackStateEvent stateEvent = new PlaybackStateEvent(request.state(), Instant.now());
        playbackStateCache.put(session.roomId(), stateEvent);

        sessionRegistry.broadcastToRoom(session.roomId(), new OutboundEvent(EventType.PLAYBACK_STATE_HINT, stateEvent));
    }
}