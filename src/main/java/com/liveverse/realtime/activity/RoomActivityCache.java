package com.liveverse.realtime.activity;

import com.liveverse.realtime.activity.dto.ActivityLogEntry;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class RoomActivityCache {

    private static final int MAX_LOG_ENTRIES = 20;

    private final Map<Long, AtomicInteger> countByRoom = new ConcurrentHashMap<>();
    private final Map<Long, Deque<ActivityLogEntry>> logByRoom = new ConcurrentHashMap<>();

    public int increment(Long roomId) {
        return countByRoom.computeIfAbsent(roomId, id -> new AtomicInteger(0)).incrementAndGet();
    }

    public int decrement(Long roomId) {
        AtomicInteger count = countByRoom.get(roomId);
        if (count == null) {
            return 0;
        }
        return Math.max(0, count.decrementAndGet());
    }

    public int getCount(Long roomId) {
        AtomicInteger count = countByRoom.get(roomId);
        return count == null ? 0 : count.get();
    }

    public void appendLog(Long roomId, ActivityLogEntry entry) {
        Deque<ActivityLogEntry> log = logByRoom.computeIfAbsent(roomId, id -> new ArrayDeque<>());
        synchronized (log) {
            log.addLast(entry);
            while (log.size() > MAX_LOG_ENTRIES) {
                log.removeFirst();
            }
        }
    }

    public List<ActivityLogEntry> getRecentLog(Long roomId) {
        Deque<ActivityLogEntry> log = logByRoom.get(roomId);
        if (log == null) {
            return List.of();
        }
        synchronized (log) {
            return List.copyOf(log);
        }
    }
}