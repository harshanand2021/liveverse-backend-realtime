package com.liveverse.realtime.client.dto;

import java.util.List;

public record PollResponse(Long pollId,
                           String title,
                           String status,
                           List<PollOptionTallyResponse> options) {
}
