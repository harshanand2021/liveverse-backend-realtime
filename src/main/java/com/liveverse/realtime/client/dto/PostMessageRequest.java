package com.liveverse.realtime.client.dto;

public record PostMessageRequest(Long senderId,
                                 String content) {
}
