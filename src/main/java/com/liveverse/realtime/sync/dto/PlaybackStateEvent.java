package com.liveverse.realtime.sync.dto;

import java.time.Instant;

public record PlaybackStateEvent(PlaybackState state,
                                 Instant changedAt) {
}
