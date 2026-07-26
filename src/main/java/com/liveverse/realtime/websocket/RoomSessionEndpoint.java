package com.liveverse.realtime.websocket;

import com.liveverse.realtime.activity.RoomActivityService;
import com.liveverse.realtime.chat.ChatHistoryService;
import com.liveverse.realtime.client.CoreRoomClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.RoomContentType;
import com.liveverse.realtime.client.dto.RoomDetailsResponse;
import com.liveverse.realtime.polling.PollStateService;
import com.liveverse.realtime.sync.PlaybackStateCache;
import com.liveverse.realtime.sync.YoutubeSyncService;
import com.liveverse.realtime.sync.dto.PlaybackStateEvent;
import com.liveverse.realtime.websocket.event.EventDispatcher;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import com.liveverse.realtime.websocket.event.InboundEvent;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@WebSocket(path = "/ws/rooms/{roomId}/users/{userId}")
public class RoomSessionEndpoint {

    private static final Logger LOG = Logger.getLogger(RoomSessionEndpoint.class);

    @Inject
    WebSocketConnection connection;

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    EventDispatcher dispatcher;

    @Inject
    ChatHistoryService chatHistoryService;

    @Inject
    PollStateService pollStateService;

    @Inject
    PlaybackStateCache playbackStateCache;

    @Inject
    YoutubeSyncService youtubeSyncService;

    @Inject
    RoomActivityService roomActivityService;

    @Inject
    @RestClient
    CoreRoomClient coreRoomClient;

    @OnOpen
    public void onOpen() {
        Long roomId = Long.valueOf(connection.pathParam("roomId"));
        Long userId = Long.valueOf(connection.pathParam("userId"));

        ApiResponseEnvelope<RoomDetailsResponse> response;
        try {
            response = coreRoomClient.getRoomDetails(roomId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch room details for room %d - closing connection %s rather than leaving it open with no session registered",
                    roomId, connection.id());
            connection.closeAndAwait();
            return;
        }

        RoomDetailsResponse room = response.data();
        if (room == null) {
            LOG.warnf("Room %d not found (or Core returned no data) - closing connection %s", roomId, connection.id());
            connection.closeAndAwait();
            return;
        }

        boolean isHost = room.hostUserId().equals(userId);

        ParticipantSession session = new ParticipantSession(
                connection.id(), userId, roomId, isHost
        );
        sessionRegistry.register(session);

        chatHistoryService.sendHistoryTo(session);
        pollStateService.sendActivePollTo(session);

        if (room.contentType() == RoomContentType.LIVE_STREAM) {
            PlaybackStateEvent currentState = playbackStateCache.get(roomId);
            if (currentState != null) {
                sessionRegistry.sendToSession(session.sessionId(), new OutboundEvent(EventType.PLAYBACK_STATE_HINT, currentState));
            }
        } else if (room.contentType() == RoomContentType.YOUTUBE) {
            youtubeSyncService.sendCurrentStateTo(session);
        }

        // Broadcasts "joined" to the room (count + log) AND sends this
        // session its own current-state snapshot (count + log backlog) -
        // two different audiences, both handled inside RoomActivityService.
        roomActivityService.participantJoined(session);
        roomActivityService.sendCurrentStateTo(session);
    }

    @OnTextMessage
    public void onMessage(InboundEvent event) {
        ParticipantSession session = sessionRegistry.get(connection.id());
        dispatcher.dispatch(session, event);
    }

    @OnClose
    public void onClose() {
        ParticipantSession session = sessionRegistry.unregister(connection.id());
        if (session != null) {
            roomActivityService.participantLeft(session);
        }
    }

    /**
     * Last-resort catch. EventDispatcher already handles the three expected
     * exception types and converts them into a client-facing error — if
     * something lands here instead, it's a genuine bug, not routine
     * business rejection, and gets logged as such rather than silently
     * swallowed.
     */
    @OnError
    public void onError(Throwable error) {
        LOG.errorf(error, "Unhandled error on connection %s", connection.id());
    }
}