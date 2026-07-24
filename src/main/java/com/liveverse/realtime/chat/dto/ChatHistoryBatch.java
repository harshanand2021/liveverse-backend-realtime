package com.liveverse.realtime.chat.dto;

import java.util.List;

public record ChatHistoryBatch(List<ChatEvent> messages) {
}
