package com.liveverse.realtime.activity.dto;

import java.time.Instant;

public record ActivityLogEntry(String message,
                               Instant timestamp) {


}
