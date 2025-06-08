package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.enums.Sort;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.InvalidDateException;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> searchEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = DateTimeUtil.DATE_PATTERN)
                                            LocalDateTime rangeStart,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(pattern = DateTimeUtil.DATE_PATTERN)
                                            LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) Sort sort,
                                            @RequestParam(defaultValue = "0") Integer from,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            HttpServletRequest request) {
        log.info("Search events - text: {}, categories: {}, paid: {}, rangeStart: {}, rangeEnd{}, onlyAvailable: {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        log.info("Client ip: {}", request.getRemoteAddr());
        log.info("Endpoint path: {}", request.getRequestURI());

        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new InvalidDateException("End date can't be before start date");
            }
        }

        return eventService.searchEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("Get event with id: {}", id);
        log.info("Client ip: {}", request.getRemoteAddr());
        log.info("Endpoint path: {}", request.getRequestURI());
        return eventService.getEventById(id, request);
    }
}
