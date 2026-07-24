package com.liveverse.realtime.websocket.event;

public enum EventType {
    CHAT,
    CHAT_HISTORY,
    POLL_CREATE,
    POLL_VOTE,
    POLL_CLOSE,
    POLL_ACTIVE,
    PLAYBACK_STATE_HINT,
    REACTION,
    WEBRTC_OFFER,
    WEBRTC_ANSWER,
    WEBRTC_ICE_CANDIDATE,
    ERROR;
}
