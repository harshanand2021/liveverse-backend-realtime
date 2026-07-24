package com.liveverse.realtime.client.dto;

public record PollOptionTallyResponse(Long optionId,
                                      String optionTitle,
                                      long voteCount) {
}
