package com.liveverse.realtime.sync;

import com.liveverse.realtime.sync.dto.PlaybackStateEvent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PlaybackStateCache {

    private final Map<Long, PlaybackStateEvent> stateByRoom = new ConcurrentHashMap<>();

    public void put(Long roomId, PlaybackStateEvent state) {
        stateByRoom.put(roomId, state);
    }

    public PlaybackStateEvent get(Long roomId) {
        return stateByRoom.get(roomId);
    }

    public void remove(Long roomId) {
        stateByRoom.remove(roomId);
    }
}