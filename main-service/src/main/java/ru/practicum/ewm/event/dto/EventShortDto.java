package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private Integer confirmedRequests;

    @JsonFormat(pattern = DateTimeUtil.DATE_PATTERN)
    private LocalDateTime eventDate;

    private UserShortDto initiator;

    private Boolean paid;

    private String title;

    private Long views;
}
