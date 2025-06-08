package ru.practicum.ewm.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class RequestPrivateController {
    private final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        log.info("Save participation request, userId: {}, eventId: {}", userId, eventId);
        ParticipationRequestDto requestDto = requestService.addRequest(userId, eventId);
        log.info("Participation request saved successfully, requestDto: {}", requestDto);
        return requestDto;
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Get requests by user with id: {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Cancel request, requestId: {}", requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUserEventRequests(@PathVariable Long userId,
                                                              @PathVariable Long eventId) {
        log.info("Get requests in user's event with id: {}, userId: {}", eventId, userId);
        return requestService.getUserEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult moderateRequests(@PathVariable Long userId,
                                                           @PathVariable Long eventId,
                                                           @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Moderate requests in user's event with id: {}, userId: {}", eventId, userId);
        return requestService.moderateRequests(userId, eventId, request);
    }
}
