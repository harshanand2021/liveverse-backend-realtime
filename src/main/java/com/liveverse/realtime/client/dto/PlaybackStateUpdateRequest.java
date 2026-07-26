package com.liveverse.realtime.client.dto;

public record PlaybackStateUpdateRequest(int positionSeconds,
                                         boolean isPlaying) {
}
