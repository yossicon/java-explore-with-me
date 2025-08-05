package ru.practicum.ewm.subscription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.subscription.dto.FeedDto;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.service.SubscriptionService;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPrivateController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto subscribe(@PathVariable Long userId,
                                     @RequestParam Long followedId) {
        log.info("POST /users/{userId}/subscriptions/{followedId}, userId: {}, followedId: {}", userId, followedId);
        return subscriptionService.subscribe(userId, followedId);
    }

    @GetMapping("/feed")
    @ResponseStatus(HttpStatus.OK)
    public List<FeedDto> getFeed(@PathVariable Long userId,
                                 @RequestParam Integer from,
                                 @RequestParam Integer size) {
        log.info("GET /users/{userId}/subscriptions/feed, userId: {}", userId);
        return subscriptionService.getFeed(userId, from, size);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getSubscriptions(@PathVariable Long userId) {
        log.info("GET /users/{userId}/subscriptions, userId: {}", userId);
        return subscriptionService.getSubscriptions(userId);
    }

    @GetMapping("/followers")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getFollowers(@PathVariable Long userId) {
        log.info("GET /users/{userId}/subscriptions/followers, userId: {}", userId);
        return subscriptionService.getFollowers(userId);
    }

    @GetMapping("/{otherId}/common")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonSubscriptions(@PathVariable Long userId,
                                                @PathVariable Long otherId) {
        log.info("GET /users/{userId}/subscriptions/{otherId}/common, userId: {}, otherId: {}", userId, otherId);
        return subscriptionService.getCommonSubscriptions(userId, otherId);
    }

    @GetMapping("/{otherId}/followers")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUserFollowers(@PathVariable Long userId,
                                          @PathVariable Long otherId) {
        log.info("GET /users/{userId}/subscriptions/{otherId}/followers, userId: {}, otherId: {}", userId, otherId);
        return subscriptionService.getUserFollowers(otherId);
    }

    @GetMapping("/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUserSubscriptions(@PathVariable Long userId,
                                              @PathVariable Long otherId) {
        log.info("GET /users/{userId}/subscriptions/{otherId}, userId: {}, otherId: {}", userId, otherId);
        return subscriptionService.getUserSubscriptions(otherId);
    }

    @DeleteMapping("/{followedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long userId,
                            @PathVariable Long followedId) {
        log.info("DELETE /users/{userId}/subscriptions/{followedId}, userId: {}, followedId: {}", userId, followedId);
        subscriptionService.unsubscribe(userId, followedId);
    }
}
