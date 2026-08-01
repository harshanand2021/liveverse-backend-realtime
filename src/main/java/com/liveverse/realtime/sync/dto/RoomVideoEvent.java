package com.liveverse.realtime.sync.dto;

/**
 * The room's video changed. Carries the new URL so seated viewers can switch
 * without re-fetching the room.
 */
public record RoomVideoEvent(String videoUrl) {
}
