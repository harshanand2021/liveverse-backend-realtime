package com.liveverse.realtime.chat;

import com.liveverse.realtime.chat.dto.ChatEvent;
import com.liveverse.realtime.chat.dto.ChatHistoryBatch;
import com.liveverse.realtime.client.CoreChatClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.ChatMessageResponse;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import com.liveverse.realtime.websocket.event.EventType;
import com.liveverse.realtime.websocket.event.OutboundEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ChatHistoryService {

    private static final Logger LOG = Logger.getLogger(ChatHistoryService.class);
    private static final int HISTORY_LIMIT = 50;

    @Inject
    @RestClient
    CoreChatClient coreChatClient;

    @Inject
    SessionRegistry sessionRegistry;

    public void sendHistoryTo(ParticipantSession session) {
        ApiResponseEnvelope<List<ChatMessageResponse>> response;
        try {
            response = coreChatClient.getRecentMessages(session.roomId(), HISTORY_LIMIT);
        } catch (Exception e) {
            LOG.warnf(e, "Failed to fetch chat history for room %d, session %s continues with none",
                    session.roomId(), session.sessionId());
            return;
        }

        if (!response.success() || response.data() == null) {
            LOG.warnf("Core rejected chat history request for room %d: %s",
                    session.roomId(), response.message());
            return;
        }

        List<ChatEvent> messages = response.data().stream()
                .map(m -> new ChatEvent(m.messageId(), m.senderId(), m.senderDisplayName(), m.content(), m.timestamp()))
                .toList();

        ChatHistoryBatch batch = new ChatHistoryBatch(messages);
        sessionRegistry.sendToSession(session.sessionId(), new OutboundEvent(EventType.CHAT_HISTORY, batch));
    }
}