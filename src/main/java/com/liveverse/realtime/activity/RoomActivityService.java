package com.liveverse.realtime.activity;

import com.liveverse.realtime.activity.dto.ActivityLogBatch;
import com.liveverse.realtime.activity.dto.ActivityLogEntry;
import com.liveverse.realtime.activity.dto.ParticipantCountEvent;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class RoomActivityService {

    @Inject
    RoomActivityCache activityCache;

    @Inject
    SessionRegistry sessionRegistry;

    public void participantJoined(ParticipantSession session) {
        int count = activityCache.increment(session.roomId());
        logAndBroadcast(session.roomId(), "User " + session.userId() + " joined");
        broadcastCount(session.roomId(), count);
    }

    public void participantLeft(ParticipantSession session) {
        int count = activityCache.decrement(session.roomId());
        logAndBroadcast(session.roomId(), "User " + session.userId() + " left");
        broadcastCount(session.roomId(), count);
    }

    public void sendCurrentStateTo(ParticipantSession session) {
        ActivityLogBatch batch = new ActivityLogBatch(activityCache.getRecentLog(session.roomId()));
        sessionRegistry.sendToSession(session.sessionId(),
                new OutboundEvent(EventType.ACTIVITY_LOG_HISTORY, batch));
    }

    private void logAndBroadcast(Long roomId, String message) {
        ActivityLogEntry entry = new ActivityLogEntry(message, Instant.now());
        activityCache.appendLog(roomId, entry);
        sessionRegistry.broadcastToRoom(roomId, new OutboundEvent(EventType.ACTIVITY_LOG, entry));
    }

    private void broadcastCount(Long roomId, int count) {
        sessionRegistry.broadcastToRoom(roomId,
                new OutboundEvent(EventType.PARTICIPANT_COUNT, new ParticipantCountEvent(count)));
    }
}