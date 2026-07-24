package com.liveverse.realtime.webrtc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.exception.TargetSessionNotFoundException;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.webrtc.dto.IceCandidate;
import com.liveverse.realtime.webrtc.dto.SdpAnswer;
import com.liveverse.realtime.webrtc.dto.SdpOffer;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebRtcSignalingEventHandler implements EventHandler {

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.WEBRTC_OFFER
                || type == EventType.WEBRTC_ANSWER
                || type == EventType.WEBRTC_ICE_CANDIDATE;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        switch (event.type()) {
            case WEBRTC_OFFER -> handleOffer(session, event);
            case WEBRTC_ANSWER -> handleAnswer(session, event);
            case WEBRTC_ICE_CANDIDATE -> handleIceCandidate(session, event);
            default -> throw new IllegalStateException("Unsupported signaling event: " + event.type());
        }
    }

    private void handleOffer(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can send a WebRTC offer");
        }

        SdpOffer offer = objectMapper.convertValue(event.payload(), SdpOffer.class);
        relay(session, offer.targetUserId(), EventType.WEBRTC_OFFER, offer);
    }

    private void handleAnswer(ParticipantSession session, InboundEvent event) {
        if (session.isHost()) {
            throw new UnauthorizedActionException("The host cannot send a WebRTC answer");
        }

        SdpAnswer answer = objectMapper.convertValue(event.payload(), SdpAnswer.class);
        relay(session, answer.targetUserId(), EventType.WEBRTC_ANSWER, answer);
    }

    private void handleIceCandidate(ParticipantSession session, InboundEvent event) {
        IceCandidate candidate = objectMapper.convertValue(event.payload(), IceCandidate.class);
        relay(session, candidate.targetUserId(), EventType.WEBRTC_ICE_CANDIDATE, candidate);
    }

    private void relay(ParticipantSession sender, Long targetUserId, EventType type, Object payload) {
        ParticipantSession target = sessionRegistry.findByUserIdInRoom(sender.roomId(), targetUserId);
        if (target == null) {
            throw new TargetSessionNotFoundException(
                    "No active session for user " + targetUserId + " in room " + sender.roomId());
        }

        sessionRegistry.sendToSession(target.sessionId(), new OutboundEvent(type, payload));
    }
}