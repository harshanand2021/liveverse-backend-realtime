package com.liveverse.realtime.reaction.dto;

public record ReactionEvent(Long userId,
                            Long messageId,
                            String reaction) {
}
