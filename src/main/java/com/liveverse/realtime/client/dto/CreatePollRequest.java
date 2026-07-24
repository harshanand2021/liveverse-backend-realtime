package com.liveverse.realtime.client.dto;

import java.util.List;

public record CreatePollRequest(Long hostUserId,
                                String title,
                                List<String> options) {
}
