package com.liveverse.realtime.webrtc.dto;

public record SdpOffer(Long targetUserId,
                       String sdp) {
}
