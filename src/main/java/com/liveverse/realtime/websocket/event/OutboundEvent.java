package com.liveverse.realtime.websocket.event;

public record OutboundEvent(EventType eventType, Object payload) {
}
