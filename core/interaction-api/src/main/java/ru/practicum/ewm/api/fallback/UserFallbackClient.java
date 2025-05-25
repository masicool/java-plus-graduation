package ru.practicum.ewm.api.fallback;

import feign.FeignException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.api.UserApi;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserFallbackClient implements UserApi {
    @Override
    public UserShortDto findById(long userId) throws FeignException {
        return UserShortDto.builder().id(userId).build();
    }

    @Override
    public List<UserShortDto> findByIdIn(List<Long> userIds) throws FeignException {
        return userIds.stream().map(userId -> UserShortDto.builder().id(userId).build()).collect(Collectors.toList());
    }
}
