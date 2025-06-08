package ru.practicum.ewm.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;

    private Boolean pinned;

    private String title;

    private Set<EventShortDto> events;
}
