package com.liveverse.realtime.websocket.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OutboundEvent(@JsonProperty("type") EventType eventType,
                            Object payload) {

}
