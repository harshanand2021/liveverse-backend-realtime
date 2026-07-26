package com.liveverse.realtime.reaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.reaction.dto.ReactionEvent;
import com.liveverse.realtime.reaction.dto.ReactionRequest;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ReactionEventHandler implements EventHandler {

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.REACTION;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        ReactionRequest request = objectMapper.convertValue(event.payload(), ReactionRequest.class);

        ReactionEvent reactionEvent = new ReactionEvent(session.userId(), request.messageId(), request.reaction());
        sessionRegistry.broadcastToRoom(session.roomId(), new OutboundEvent(EventType.REACTION, reactionEvent));
    }
}