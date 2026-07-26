package com.liveverse.realtime.client.dto;

public record RoomDetailsResponse(Long roomId,
                                  Long hostUserId,
                                  String status,
                                  RoomContentType contentType) {
}
