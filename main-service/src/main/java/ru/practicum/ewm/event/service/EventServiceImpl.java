package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitSaveDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.enums.Sort;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.AccessDeniedException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.InvalidDateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventDto addEvent(Long userId, EventSaveDto eventSaveDto) {
        User initiator = findUserById(userId);
        Category category = findCategoryById(eventSaveDto.getCategory());

        if (eventSaveDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new InvalidDateException("Event can't start earlier than 2 hours from now");
        }

        Event event = eventMapper.mapToEvent(eventSaveDto);
        event.setCategory(category);
        event.setConfirmedRequests(0);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);

        Location location = locationRepository.save(locationMapper.mapToLocation(eventSaveDto.getLocation()));
        event.setLocation(location);
        event.setCategory(category);

        if (eventSaveDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (eventSaveDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (eventSaveDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        return eventMapper.mapToEventDto(eventRepository.save(event));
    }

    @Override
    public List<EventDto> getUserEvents(Long userId, Integer from, Integer size) {
        return eventRepository.findUserEventsLimited(userId, size, from).stream()
                .map(eventMapper::mapToEventDto)
                .toList();
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Integer from, Integer size) {
        List<Event> events = eventRepository.findEventsFiltered(
                users, states, categories, rangeStart, rangeEnd, from, size
        );


        return events.stream()
                .map(eventMapper::mapToEventDto)
                .toList();
    }

    @Override
    public EventDto getUserEventById(Long userId, Long eventId) {
        Event event = findEventById(eventId);
        return eventMapper.mapToEventDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEventByUser(Long userId, Long eventId, EventUpdateUserDto eventUpdate) {
        Event event = findEventById(eventId);
        if (!userId.equals(event.getInitiator().getId())) {
            throw new AccessDeniedException("Only initiator can update event");
        }
        if (!State.PENDING.equals(event.getState()) && !State.CANCELED.equals(event.getState())) {
            throw new ConflictException("Only pending or canceled event can be updated");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new InvalidDateException("Can't update event because it starts in less than 2 hours");
        }

        eventMapper.updateEventFromUserRequest(eventUpdate, event);

        if (eventUpdate.getCategory() != null) {
            Category category = findCategoryById(eventUpdate.getCategory());
            event.setCategory(category);
        }

        if (eventUpdate.getStateAction() != null) {
            switch (eventUpdate.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("State action is not supported");
            }
        }

        return eventMapper.mapToEventDto(event);
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, EventUpdateAdminDto eventUpdate) {
        Event event = findEventById(eventId);

        if (eventUpdate.getEventDate() != null
                && event.getEventDate().isBefore(LocalDateTime.now().plusHours(1L))) {
            throw new InvalidDateException("Can't update event because it starts in less than 1 hour");
        }

        eventMapper.updateEventFromAdminRequest(eventUpdate, event);

        if (eventUpdate.getCategory() != null) {
            Category category = findCategoryById(eventUpdate.getCategory());
            event.setCategory(category);
        }

        if (eventUpdate.getStateAction() != null) {
            if (State.PENDING.equals(event.getState())) {
                switch (eventUpdate.getStateAction()) {
                    case PUBLISH_EVENT:
                        event.setState(State.PUBLISHED);
                        event.setPublishedOn(LocalDateTime.now());
                        break;
                    case REJECT_EVENT:
                        event.setState(State.CANCELED);
                        break;
                    default:
                        throw new IllegalArgumentException("State action is not supported");
                }
            } else {
                throw new ConflictException("Event must be pending for status moderation");
            }
        }
        return eventMapper.mapToEventDto(event);
    }

    @Override
    public EventDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = findEventById(eventId);
        if (!State.PUBLISHED.equals(event.getState())) {
            throw new NotFoundException(String.format("Event with id %d not found", eventId));
        }
        sendStats(request);
        EventDto eventDto = eventMapper.mapToEventDto(event);
        eventDto.setViews(getEventViews(event));
        return eventDto;
    }

    @Override
    public List<EventShortDto> searchEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, Sort sort, Integer from,
                                            Integer size, HttpServletRequest request) {
        List<Event> events = eventRepository.searchEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size
        );
        sendStats(request);
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> {
                    EventShortDto eventShortDto = eventMapper.mapToEventShortDto(event);
                    eventShortDto.setViews(getEventViews(event));
                    return eventShortDto;
                })
                .toList();
        return sortEvents(eventShortDtos, sort);
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %d not found", categoryId)));
    }

    private Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found", eventId)));

    }

    private void sendStats(HttpServletRequest request) {
        EndpointHitSaveDto hitSaveDto = new EndpointHitSaveDto(
                "ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        statsClient.saveHit(hitSaveDto);
    }

    private Long getEventViews(Event event) {
        List<ViewStatsDto> stats = statsClient.getStats(
                event.getPublishedOn(),
                LocalDateTime.now(),
                List.of("/events/" + event.getId()),
                true
        );
        return stats.isEmpty() ? 0 : stats.getFirst().getHits();
    }

    private List<EventShortDto> sortEvents(List<EventShortDto> eventShortDtos, Sort sort) {
        return switch (sort) {
            case VIEWS -> eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed()).toList();
            case EVENT_DATE -> eventShortDtos.stream()
                    .sorted(Comparator.comparing(EventShortDto::getEventDate).reversed()).toList();
            case null -> eventShortDtos;
        };
    }
}

