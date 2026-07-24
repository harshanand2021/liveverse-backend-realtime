package com.liveverse.realtime.webrtc.dto;

public record IceCandidate(Long targetUserId,
                           String candidate,
                           String sdpMid,
                           Integer sdpMLineIndex) {
}
