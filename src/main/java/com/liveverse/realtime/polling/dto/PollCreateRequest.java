package com.liveverse.realtime.polling.dto;

import java.util.List;

public record PollCreateRequest(String title,
                                List<String> options) {
}
