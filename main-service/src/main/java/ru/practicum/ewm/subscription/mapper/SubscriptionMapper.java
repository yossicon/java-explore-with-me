package ru.practicum.ewm.subscription.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface SubscriptionMapper {
    SubscriptionDto mapToSubscriptionDto(Subscription subscription);
}
