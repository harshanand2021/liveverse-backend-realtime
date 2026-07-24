package com.liveverse.realtime.polling.dto;

public record PollOptionTally(Long optionId,
                              String optionTitle,
                              long voteCount){
}
