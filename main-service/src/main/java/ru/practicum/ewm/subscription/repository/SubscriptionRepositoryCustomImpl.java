package ru.practicum.ewm.subscription.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.request.model.QParticipationRequest;
import ru.practicum.ewm.subscription.dto.FeedDto;
import ru.practicum.ewm.user.model.QUser;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SubscriptionRepositoryCustomImpl implements SubscriptionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FeedDto> findFollowedUsersEvents(List<Long> users, Integer from, Integer size) {
        QEvent event = QEvent.event;
        QUser user = QUser.user;
        QParticipationRequest request = QParticipationRequest.participationRequest;
        return jpaQueryFactory
                .select(Projections.constructor(
                        FeedDto.class,
                        user.id,
                        user.name,
                        event.id,
                        event.title,
                        event.eventDate
                ))
                .from(event)
                .join(request).on(request.event.id.eq(event.id))
                .join(user).on(request.requester.id.eq(user.id))
                .where(
                        request.requester.id.in(users),
                        request.isPublic.eq(true),
                        request.status.eq(State.CONFIRMED),
                        event.eventDate.after(LocalDateTime.now())
                )
                .orderBy(event.eventDate.asc())
                .offset(from)
                .limit(size)
                .fetch();
    }
}
