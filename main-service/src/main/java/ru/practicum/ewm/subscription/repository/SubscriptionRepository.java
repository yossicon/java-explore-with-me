package ru.practicum.ewm.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.subscription.model.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {
    Optional<Subscription> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("""
            select s.followed.id
            from Subscription s
            where s.follower.id = :userId
            """)
    List<Long> findFollowedUserIds(@Param("userId") Long userId);

    @Query("""
            select s.follower.id
            from Subscription s
            where s.followed.id = :userId
            """)
    List<Long> findFollowersIds(@Param("userId") Long userId);

    @Query("""
                select s1.followed.id
                from Subscription s1
                join Subscription s2 on s1.followed.id = s2.followed.id
                where s1.follower.id = :user
                and s2.follower.id = :other
            """)
    List<Long> findCommonFollowedUsersIds(@Param("user") Long userId,
                                          @Param("other") Long otherId);
}
