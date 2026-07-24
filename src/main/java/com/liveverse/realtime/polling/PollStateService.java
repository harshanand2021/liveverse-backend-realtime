package com.liveverse.realtime.polling;

import com.liveverse.realtime.client.CorePollClient;
import com.liveverse.realtime.client.dto.ApiResponseEnvelope;
import com.liveverse.realtime.client.dto.PollResponse;
import com.liveverse.realtime.polling.dto.PollEvent;
import com.liveverse.realtime.polling.dto.PollOptionTally;
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
public class PollStateService {

    private static final Logger LOG = Logger.getLogger(PollStateService.class);

    @Inject
    @RestClient
    CorePollClient corePollClient;

    @Inject
    SessionRegistry sessionRegistry;

    public void sendActivePollTo(ParticipantSession session) {
        ApiResponseEnvelope<PollResponse> response;
        try {
            response = corePollClient.getActivePoll(session.roomId());
        } catch (Exception e) {
            LOG.warnf(e, "Failed to fetch active poll for room %d, session %s continues without one",
                    session.roomId(), session.sessionId());
            return;
        }

        if (!response.success() || response.data() == null) {
            // No active poll is a normal state, not a failure - nothing to send.
            return;
        }

        PollResponse poll = response.data();
        List<PollOptionTally> tallies = poll.options().stream()
                .map(o -> new PollOptionTally(o.optionId(), o.optionTitle(), o.voteCount()))
                .toList();
        PollEvent pollEvent = new PollEvent(poll.pollId(), poll.title(), poll.status(), tallies);

        sessionRegistry.sendToSession(session.sessionId(), new OutboundEvent(EventType.POLL_ACTIVE, pollEvent));
    }
}