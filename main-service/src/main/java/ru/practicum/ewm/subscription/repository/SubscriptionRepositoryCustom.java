package ru.practicum.ewm.subscription.repository;

import ru.practicum.ewm.subscription.dto.FeedDto;

import java.util.List;

public interface SubscriptionRepositoryCustom {
    List<FeedDto> findFollowedUsersEvents(List<Long> users, Integer from, Integer size);
}
