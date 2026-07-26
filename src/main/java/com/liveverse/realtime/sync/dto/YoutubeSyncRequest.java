package com.liveverse.realtime.sync.dto;

public record YoutubeSyncRequest(YoutubeAction action,
                                 int positionSeconds,
                                 boolean isPlaying) {
}
