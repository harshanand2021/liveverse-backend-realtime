package com.liveverse.realtime.client.dto;

import java.time.Instant;

public record PlaybackStateResponse(String videoUrl,
                                    int positionSeconds,
                                    boolean isPlaying,
                                    Instant updatedAt) {
}
