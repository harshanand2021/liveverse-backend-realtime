package com.liveverse.realtime.websocket;

import com.liveverse.realtime.chat.ChatHistoryService;
import com.liveverse.realtime.client.CoreRoomClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.RoomDetailsResponse;
import com.liveverse.realtime.polling.PollStateService;
import com.liveverse.realtime.sync.PlaybackStateCache;
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
    @RestClient
    CoreRoomClient coreRoomClient;

    @OnOpen
    public void onOpen() {
        Long roomId = Long.valueOf(connection.pathParam("roomId"));
        Long userId = Long.valueOf(connection.pathParam("userId"));

        ApiResponseEnvelope<RoomDetailsResponse> response = coreRoomClient.getRoomDetails(roomId);
        RoomDetailsResponse room = response.data();

        boolean isHost = room.hostUserId().equals(userId);

        ParticipantSession session = new ParticipantSession(
                connection.id(), userId, roomId, isHost
        );
        sessionRegistry.register(session);

        chatHistoryService.sendHistoryTo(session);
        pollStateService.sendActivePollTo(session);

        PlaybackStateEvent currentState = playbackStateCache.get(roomId);
        if (currentState != null) {
            sessionRegistry.sendToSession(session.sessionId(), new OutboundEvent(EventType.PLAYBACK_STATE_HINT, currentState));
        }
    }

    @OnTextMessage
    public void onMessage(InboundEvent event) {
        ParticipantSession session = sessionRegistry.get(connection.id());
        dispatcher.dispatch(session, event);
    }

    @OnClose
    public void onClose() {
        sessionRegistry.unregister(connection.id());
    }

    @OnError
    public void onError(Throwable error) {
        LOG.errorf(error, "Unhandled error on connection %s", connection.id());
    }
}