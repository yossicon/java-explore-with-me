package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventDto;
import ru.practicum.ewm.event.dto.EventSaveDto;
import ru.practicum.ewm.event.dto.EventUpdateUserDto;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable Long userId,
                             @RequestBody @Valid EventSaveDto eventSaveDto) {
        log.info("Save event {}", eventSaveDto);
        EventDto eventDto = eventService.addEvent(userId, eventSaveDto);
        log.info("Event saved successfully, eventDto: {}", eventDto);
        return eventDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getUserEvents(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("Get events - userId: {}, from: {}, size: {}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEventById(@PathVariable Long userId,
                                 @PathVariable Long eventId) {
        log.info("Get user event by id {}", eventId);
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto updateEvent(@PathVariable Long userId,
                                @PathVariable Long eventId,
                                @RequestBody @Valid EventUpdateUserDto eventUpdateDto) {
        log.info("Update event {}", eventUpdateDto);
        EventDto eventDto = eventService.updateEventByUser(userId, eventId, eventUpdateDto);
        log.info("Event updated successfully, eventDto: {}", eventDto);
        return eventDto;
    }
}
