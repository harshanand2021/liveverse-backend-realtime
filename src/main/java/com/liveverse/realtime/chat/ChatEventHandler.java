package com.liveverse.realtime.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liveverse.realtime.chat.dto.ChatEvent;
import com.liveverse.realtime.chat.dto.ChatMessageRequest;
import com.liveverse.realtime.client.CoreChatClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.ChatMessageResponse;
import com.liveverse.realtime.client.dto.PostMessageRequest;
import com.liveverse.realtime.exception.CoreServiceException;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventHandler;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.InboundEvent;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Persist-then-broadcast, in that order - unlike ReactionEventHandler,
 * ChatEvent needs the server-assigned messageId and timestamp that only
 * exist after Core's write completes, so there's genuinely nothing correct
 * to broadcast until persistence finishes.
 */
@ApplicationScoped
public class ChatEventHandler implements EventHandler {

    @Inject
    @RestClient
    CoreChatClient coreChatClient;

    @Inject
    SessionRegistry sessionRegistry;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public boolean supports(EventType type) {
        return type == EventType.CHAT;
    }

    @Override
    public void handle(ParticipantSession session, InboundEvent event) {
        ChatMessageRequest request = objectMapper.convertValue(event.payload(), ChatMessageRequest.class);

        ApiResponseEnvelope<ChatMessageResponse> response;
        try {
            response = coreChatClient.postMessage(
                    session.roomId(),
                    new PostMessageRequest(session.userId(), request.content())
            );
        } catch (Exception e) {
            throw new CoreServiceException("Failed to post message", e);
        }

        if (!response.success() || response.data() == null) {
            throw new CoreServiceException(
                    response.message() != null ? response.message() : "Message rejected"
            );
        }

        ChatMessageResponse posted = response.data();
        ChatEvent chatEvent = new ChatEvent(
                posted.messageId(),
                posted.senderId(),
                posted.senderDisplayName(),
                posted.content(),
                posted.timestamp()
        );

        sessionRegistry.broadcastToRoom(session.roomId(), new OutboundEvent(EventType.CHAT, chatEvent));
    }
}