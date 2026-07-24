package com.liveverse.realtime.websocket.event;

import com.liveverse.realtime.exception.CoreServiceException;
import com.liveverse.realtime.exception.TargetSessionNotFoundException;
import com.liveverse.realtime.exception.UnauthorizedActionException;
import com.liveverse.realtime.websocket.ParticipantSession;
import com.liveverse.realtime.websocket.SessionRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class EventDispatcher {

    @Inject
    Instance<EventHandler> handlers;

    @Inject
    SessionRegistry sessionRegistry;

    public void dispatch(ParticipantSession session, InboundEvent event) {
        try {
            for (EventHandler handler : handlers) {
                if (handler.supports(event.type())) {
                    handler.handle(session, event);
                    return;
                }
            }
        } catch (UnauthorizedActionException | TargetSessionNotFoundException | CoreServiceException e) {
            OutboundEvent errorEvent = new OutboundEvent(EventType.ERROR, new ErrorEvent(e.getMessage()));
            sessionRegistry.sendToSession(session.sessionId(), errorEvent);
        }
    }
}