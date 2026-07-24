package com.liveverse.realtime.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Optional;

/**
 * ICE server configuration for WebRTC signaling.
 *
 * Defaults to Google's free public STUN servers - sufficient for direct
 * peer-to-peer connections in the common case, and reasonable at
 * CDAC-project scale. This is a DEFAULT, not a locked decision - nobody
 * chose this deliberately, it's just a sensible starting point so `config`
 * isn't left unwritten.
 *
 * TURN fields are Optional and unset by default, since no TURN provider
 * has been picked. TURN only becomes necessary if testing shows viewers
 * behind restrictive/symmetric NATs that STUN alone can't get through -
 * fill these in if and when that actually happens, no code change needed.
 *
 * Bound from application.properties:
 *   webrtc.ice-servers=stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302
 *   webrtc.turn-url=turn:your-turn-server:3478       (optional)
 *   webrtc.turn-username=...                          (optional)
 *   webrtc.turn-credential=...                        (optional)
 *
 * NOTE - genuinely open integration question, not solved here: this data
 * needs to reach the BROWSER, since it's the frontend that constructs the
 * RTCPeerConnection, not Quarkus. Nothing in this project yet exposes
 * WebRtcConfig's values to the client (no endpoint, no field on connect).
 * That's a real remaining piece, not just a frontend detail - flagging it
 * rather than quietly leaving it undiscovered.
 */
@ConfigMapping(prefix = "webrtc")
public interface WebRtcConfig {

    @WithDefault("stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302")
    List<String> iceServers();

    Optional<String> turnUrl();

    Optional<String> turnUsername();

    Optional<String> turnCredential();
}