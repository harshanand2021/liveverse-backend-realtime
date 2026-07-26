package com.liveverse.realtime.activity.dto;

import java.util.List;

public record ActivityLogBatch(List<ActivityLogEntry> entries) {
}
