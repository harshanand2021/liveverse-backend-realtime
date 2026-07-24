package com.liveverse.realtime.chat.dto;

import java.time.Instant;

public record ChatEvent(Long messageId,
                        Long senderId,
                        String senderDisplayName,
                        String content,
                        Instant timestamp) {
}
