package com.liveverse.realtime.polling.dto;

import java.util.List;

public record PollEvent(Long pollId,
                        String title,
                        String status,
                        List<PollOptionTally> options) {
}
