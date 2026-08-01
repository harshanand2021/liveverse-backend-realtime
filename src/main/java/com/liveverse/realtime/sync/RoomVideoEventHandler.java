package com.liveverse.realtime.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.sync.dto.RoomVideoEvent;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Pushes a room's new video to everyone already seated, so they switch without
 * having to refresh.
 *
 * Deliberately broadcast-only: unlike {@link YoutubeSyncEventHandler}, this does
 * not call Core. The host's browser has already persisted the change through the
 * authenticated PATCH /api/rooms/{id}/video, which is what proves they own the
 * room. Persisting here as well would need that endpoint opened up to
 * unauthenticated service calls, so the host check below guards the broadcast
 * only — a viewer cannot use this to move the room off its video.
 */
@ApplicationScoped
public class RoomVideoEventHandler implements EventHandler {

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.ROOM_VIDEO_CHANGED;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can change the video");
        }

        RoomVideoEvent payload = objectMapper.convertValue(event.payload(), RoomVideoEvent.class);
        sessionRegistry.broadcastToRoom(
                session.roomId(), new OutboundEvent(EventType.ROOM_VIDEO_CHANGED, payload));
    }
}
