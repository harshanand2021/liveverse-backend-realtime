package com.liveverse.realtime.sync;

import com.liveverse.realtime.client.CorePlaybackClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.PlaybackStateResponse;
import com.liveverse.realtime.sync.dto.YoutubeSyncEvent;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class YoutubeSyncService {

    private static final Logger LOG = Logger.getLogger(YoutubeSyncService.class);

    @Inject
    @RestClient
    CorePlaybackClient corePlaybackClient;

    @Inject
    SessionRegistry sessionRegistry;

    public void sendCurrentStateTo(ParticipantSession session) {
        ApiResponseEnvelope<PlaybackStateResponse> response;
        try {
            response = corePlaybackClient.getPlaybackState(session.roomId());
        } catch (Exception e) {
            LOG.warnf(e, "Failed to fetch YouTube playback state for room %d, session %s continues without one",
                    session.roomId(), session.sessionId());
            return;
        }

        if (!response.success() || response.data() == null) {
            return;
        }

        PlaybackStateResponse state = response.data();
        YoutubeSyncEvent syncEvent = new YoutubeSyncEvent(state.isPlaying(), state.positionSeconds(), state.updatedAt());

        sessionRegistry.sendToSession(session.sessionId(),
                new OutboundEvent(EventType.YOUTUBE_SYNC_STATE, syncEvent));
    }
}