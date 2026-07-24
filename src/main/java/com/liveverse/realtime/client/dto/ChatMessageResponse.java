package com.liveverse.realtime.client.dto;

import java.time.Instant;

public record ChatMessageResponse(Long messageId,
                                  Long senderId,
                                  String senderDisplayName,
                                  String content,
                                  Instant timestamp) {
}
