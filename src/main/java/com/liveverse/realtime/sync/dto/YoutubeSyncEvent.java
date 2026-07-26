package com.liveverse.realtime.sync.dto;

import java.time.Instant;

public record YoutubeSyncEvent(boolean isPlaying,
                               int positionSeconds,
                               Instant updatedAt) {
}
