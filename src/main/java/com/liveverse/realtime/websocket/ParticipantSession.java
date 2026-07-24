package com.liveverse.realtime.websocket;


public record ParticipantSession(
        String sessionId,
        Long userId,
        Long roomId,
        boolean isHost
) {
}