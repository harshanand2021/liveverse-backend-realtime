package com.liveverse.realtime.websocket.event;

import com.fasterxml.jackson.databind.JsonNode;

public record InboundEvent(
        EventType type,
        JsonNode payload
) {
}