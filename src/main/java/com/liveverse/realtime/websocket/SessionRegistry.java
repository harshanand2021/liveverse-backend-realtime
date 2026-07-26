package com.liveverse.realtime.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory only — this is deliberately not backed by any database. Holds
 * two kinds of state: which {@link ParticipantSession} belongs to which
 * room (for broadcastToRoom), and lets any of them be looked up by user
 * (for the pairwise WebRTC signaling case).
 *
 * Delivery goes through {@code OpenConnections}, not a cached
 * {@code WebSocketConnection} field, since only connections obtained that
 * way are safe to use outside their own originating callback.
 */
@ApplicationScoped
public class SessionRegistry {

    @Inject
    OpenConnections openConnections;

    @Inject
    ObjectMapper objectMapper;

    private final Map<String, ParticipantSession> sessions = new ConcurrentHashMap<>();

    public void register(ParticipantSession session) {
        sessions.put(session.sessionId(), session);
    }

    public ParticipantSession unregister(String sessionId) {
        return sessions.remove(sessionId);
    }

    public ParticipantSession get(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Used by WebRTC signaling to resolve a {@code targetUserId} to an
     * active session within the same room.
     */
    public ParticipantSession findByUserIdInRoom(Long roomId, Long userId) {
        return sessions.values().stream()
                .filter(s -> s.roomId().equals(roomId) && s.userId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    public void broadcastToRoom(Long roomId, OutboundEvent event) {
        String json = serialize(event);
        sessions.values().stream()
                .filter(s -> s.roomId().equals(roomId))
                .forEach(s -> sendRaw(s.sessionId(), json));
    }

    public void sendToSession(String sessionId, OutboundEvent event) {
        sendRaw(sessionId, serialize(event));
    }

    private void sendRaw(String sessionId, String json) {
        openConnections.stream()
                .filter(WebSocketConnection::isOpen)
                .filter(conn -> conn.id().equals(sessionId))
                .findFirst()
                .ifPresent(conn -> conn.sendTextAndAwait(json));
    }

    private String serialize(OutboundEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbound event", e);
        }
    }
}