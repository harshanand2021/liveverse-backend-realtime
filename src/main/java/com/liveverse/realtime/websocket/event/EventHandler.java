package com.liveverse.realtime.websocket.event;

import com.liveverse.realtime.websocket.ParticipantSession;

public interface EventHandler {
    boolean supports(EventType type);
    void handle(ParticipantSession session, InboundEvent event)
}
