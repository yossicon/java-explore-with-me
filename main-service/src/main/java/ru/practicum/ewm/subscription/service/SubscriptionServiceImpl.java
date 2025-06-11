package ru.practicum.ewm.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.DuplicatedDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.subscription.dto.FeedDto;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.mapper.SubscriptionMapper;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public SubscriptionDto subscribe(Long userId, Long followedId) {
        log.info("Subscribing to user");
        if (userId.equals(followedId)) {
            log.warn("Self-follow attempt by user with id {}", userId);
            throw new ConflictException("User can't subscribe to themselves");
        }
        if (subscriptionRepository.findByFollowerIdAndFollowedId(userId, followedId).isPresent()) {
            log.warn("User {} already follows user {}", userId, followedId);
            throw new DuplicatedDataException("Subscription already exists");
        }

        User follower = findUserById(userId);
        User followed = findUserById(followedId);

        Subscription subscription = new Subscription();
        subscription.setFollower(follower);
        subscription.setFollowed(followed);
        SubscriptionDto subscriptionDto = subscriptionMapper.mapToSubscriptionDto(
                subscriptionRepository.save(subscription)
        );
        log.info("Subscription saved successfully, subscriptionDto: {}", subscriptionDto);
        return subscriptionDto;
    }

    @Override
    public List<FeedDto> getFeed(Long userId, Integer from, Integer size) {
        log.info("Search followed users' participation in events");
        List<Long> followedIds = subscriptionRepository.findFollowedUserIds(userId);
        List<FeedDto> feedDtos = subscriptionRepository.findFollowedUsersEvents(followedIds, from, size);
        log.info("{} users' participation in events were found", feedDtos.size());
        return feedDtos;
    }

    @Override
    public List<UserDto> getSubscriptions(Long userId) {
        log.info("Search current user's subscriptions");
        List<Long> followedIds = subscriptionRepository.findFollowedUserIds(userId);
        List<User> followed = userRepository.findAllByIdIn(followedIds);
        log.info("{} followed users by current user {} were found", userId, followed.size());
        return followed.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public List<UserDto> getFollowers(Long userId) {
        log.info("Search current user's followers");
        List<Long> followerIds = subscriptionRepository.findFollowersIds(userId);
        List<User> followers = userRepository.findAllByIdIn(followerIds);
        log.info("{} followers were found for current user {}", followers.size(), userId);
        return followers.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public List<UserDto> getCommonSubscriptions(Long userId, Long otherId) {
        log.info("Search common subscriptions with user");
        findUserById(otherId);
        List<Long> commonFollowedIds = subscriptionRepository.findCommonFollowedUsersIds(userId, otherId);
        List<User> common = userRepository.findAllByIdIn(commonFollowedIds);
        log.info(
                "{} common followed users were found for user {} and user {}", common.size(), userId, otherId
        );
        return common.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public List<UserDto> getUserFollowers(Long otherId) {
        log.info("Search other user's followers");
        findUserById(otherId);
        List<Long> followerIds = subscriptionRepository.findFollowersIds(otherId);
        List<User> followers = userRepository.findAllByIdIn(followerIds);
        log.info("{} user's {} followers were found", otherId, followers.size());
        return followers.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    public List<UserDto> getUserSubscriptions(Long otherId) {
        log.info("Search other user's subscriptions");
        findUserById(otherId);
        List<Long> followedIds = subscriptionRepository.findFollowedUserIds(otherId);
        List<User> followed = userRepository.findAllByIdIn(followedIds);
        log.info("{} followed users by user {}", followed.size(), otherId);
        return followed.stream()
                .map(userMapper::mapToUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, Long followedId) {
        log.info("Unsubscribing from user");
        Subscription subscription = checkSubscription(userId, followedId);
        subscriptionRepository.deleteById(subscription.getId());
        log.info("Subscription by user {} canceled successfully", userId);

    }

    private User findUserById(Long userId) {
        log.debug("Search user with id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id {} not found", userId);
                    return new NotFoundException(String.format("User with id %d not found", userId));
                });
    }

    private Subscription checkSubscription(Long followerId, Long followedId) {
        log.debug("Check subscription, followerId: {}, followedId: {}", followerId, followedId);
        return subscriptionRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> {
                    log.warn("Subscription by user {} not found", followerId);
                    return new NotFoundException(String.format("Subscription by user %d not found", followerId));
                });
    }
}
