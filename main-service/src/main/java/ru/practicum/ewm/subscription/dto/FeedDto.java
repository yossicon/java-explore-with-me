package ru.practicum.ewm.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedDto {
    private Long userId;

    private String userName;

    private Long eventId;

    private String eventTitle;

    private LocalDateTime eventDate;
}
