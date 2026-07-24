package com.liveverse.realtime.client.dto;

import java.time.Instant;

public record ApiResponseEnvelope<T>(boolean success,
                                     T data,
                                     String message,
                                     Instant timestamp) {
}
