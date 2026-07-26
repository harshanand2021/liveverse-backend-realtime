package com.liveverse.realtime.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.client.CorePlaybackClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.PlaybackStateResponse;
import com.liveverse.realtime.client.dto.PlaybackStateUpdateRequest;
import com.liveverse.realtime.exception.CoreServiceException;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.sync.dto.YoutubeSyncEvent;
import com.liveverse.realtime.sync.dto.YoutubeSyncRequest;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class YoutubeSyncEventHandler implements EventHandler {

    @Inject
    @RestClient
    CorePlaybackClient corePlaybackClient;

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.YOUTUBE_PLAY
                || type == EventType.YOUTUBE_PAUSE
                || type == EventType.YOUTUBE_SEEK;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        if (!session.isHost()) {
            throw new UnauthorizedActionException("Only the host can control YouTube playback");
        }

        YoutubeSyncRequest request = objectMapper.convertValue(event.payload(), YoutubeSyncRequest.class);

        ApiResponseEnvelope<PlaybackStateResponse> response;
        try {
            response = corePlaybackClient.updatePlaybackState(
                    session.roomId(),
                    new PlaybackStateUpdateRequest(request.positionSeconds(), request.isPlaying())
            );
        } catch (Exception e) {
            throw new CoreServiceException("Failed to update YouTube playback state", e);
        }

        if (!response.success() || response.data() == null) {
            throw new CoreServiceException(
                    response.message() != null ? response.message() : "Playback update rejected"
            );
        }

        PlaybackStateResponse state = response.data();
        YoutubeSyncEvent syncEvent = new YoutubeSyncEvent(state.isPlaying(), state.positionSeconds(), state.updatedAt());

        sessionRegistry.broadcastToRoom(session.roomId(), new OutboundEvent(event.type(), syncEvent));
    }
}