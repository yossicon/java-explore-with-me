package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventUpdateAdminDto;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.InvalidDateException;
import ru.practicum.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getEvents(@RequestParam(required = false) List<Long> users,
                                    @RequestParam(required = false) List<State> states,
                                    @RequestParam(required = false) List<Long> categories,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(pattern = DateTimeUtil.DATE_PATTERN)
                                    LocalDateTime rangeStart,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(pattern = DateTimeUtil.DATE_PATTERN)
                                    LocalDateTime rangeEnd,
                                    @RequestParam(defaultValue = "0") Integer from,
                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get events - users: {}, states: {}, categories: {}, rangeStart: {}, rangeEnd: {}, from: {}, size: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new InvalidDateException("End date can't be before start date");
            }
        }

        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEvent(@PathVariable Long eventId,
                                @RequestBody @Valid EventUpdateAdminDto eventUpdateDto) {
        log.info("Update event by admin {}", eventUpdateDto);
        EventDto eventDto = eventService.updateEventByAdmin(eventId, eventUpdateDto);
        log.info("Event updated successfully, eventDto: {}", eventDto);
        return eventDto;
    }
}
