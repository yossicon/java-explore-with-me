package ru.practicum.ewm.subscription.service;

import ru.practicum.ewm.subscription.dto.FeedDto;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface SubscriptionService {
    SubscriptionDto subscribe(Long userId, Long followedId);

    List<FeedDto> getFeed(Long userId, Integer from, Integer size);

    List<UserDto> getSubscriptions(Long userId);

    List<UserDto> getFollowers(Long userId);

    List<UserDto> getCommonSubscriptions(Long userId, Long otherId);

    List<UserDto> getUserFollowers(Long otherId);

    List<UserDto> getUserSubscriptions(Long otherId);

    void unsubscribe(Long userId, Long followedId);
}
