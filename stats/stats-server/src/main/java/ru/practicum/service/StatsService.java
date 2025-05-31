package ru.practicum.service;

import ru.practicum.EndpointHitDto;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    EndpointHitDto saveHit(EndpointHitSaveDto hitSaveDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
