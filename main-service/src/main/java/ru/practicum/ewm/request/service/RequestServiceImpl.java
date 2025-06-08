package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User requester = findUserById(userId);
        Event event = findEventById(eventId);

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new DuplicatedDataException(String.format(
                    "User with id %d has already submitted a request for event with id %d", userId, eventId)
            );
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new AccessDeniedException("Event initiator can't submit a request to their own event");
        }
        if (!State.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Only published events are available for requests");
        }
        checkParticipantLimit(event);

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setCreated(LocalDateTime.now());
        participationRequest.setEvent(event);
        participationRequest.setRequester(requester);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(State.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            participationRequest.setStatus(State.PENDING);
        }

        return requestMapper.mapToRequestDto(requestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id %d not found", requestId)));

        if (!userId.equals(participationRequest.getRequester().getId())) {
            throw new AccessDeniedException("Only requester can cancel request");
        }
        participationRequest.setStatus(State.CANCELED);

        return requestMapper.mapToRequestDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        findEventById(eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult moderateRequests(Long userId,
                                                           Long eventId,
                                                           EventRequestStatusUpdateRequest request) {
        Event event = findEventById(eventId);

        if (!userId.equals(event.getInitiator().getId())) {
            throw new AccessDeniedException("Only initiator can moderate requests");
        }
        checkParticipantLimit(event);

        List<ParticipationRequest> requests = requestRepository.findAllByIdInAndStatus(
                request.getRequestIds(), State.PENDING
        );

        if (requests.isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            return moderateAll(State.CONFIRMED, requests);
        }
        if (State.REJECTED.equals(request.getStatus())) {
            return moderateAll(State.REJECTED, requests);
        }

        int availableCount = event.getParticipantLimit() - event.getConfirmedRequests();
        List<ParticipationRequest> confirmed = requests.stream()
                .limit(availableCount)
                .peek(r -> r.setStatus(State.CONFIRMED))
                .toList();
        List<ParticipationRequest> rejected = requests.stream()
                .skip(availableCount)
                .peek(r -> r.setStatus(State.REJECTED))
                .toList();

        requestRepository.saveAll(confirmed);
        requestRepository.saveAll(rejected);
        event.setConfirmedRequests(event.getConfirmedRequests() + confirmed.size());

        return new EventRequestStatusUpdateResult(
                requestMapper.mapToListRequestDto(confirmed),
                requestMapper.mapToListRequestDto(rejected)
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found", eventId)));

    }

    private void checkParticipantLimit(Event event) {
        Integer participantLimit = event.getParticipantLimit();

        if (participantLimit != 0 && event.getConfirmedRequests() >= participantLimit) {
            throw new ConflictException(String.format("Event with id %d has reached participant limit", event.getId()));
        }
    }

    private EventRequestStatusUpdateResult moderateAll(State state, List<ParticipationRequest> requests) {
        if (State.CONFIRMED.equals(state)) {
            requests.forEach(r -> r.setStatus(State.CONFIRMED));
            return new EventRequestStatusUpdateResult(requestMapper.mapToListRequestDto(requests), List.of());
        } else {
            requests.forEach(r -> r.setStatus(State.REJECTED));
            return new EventRequestStatusUpdateResult(List.of(), requestMapper.mapToListRequestDto(requests));
        }
    }
}
