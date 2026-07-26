package com.liveverse.realtime.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record InboundEvent(
        @JsonProperty("type") EventType type,
        JsonNode payload
) {
}