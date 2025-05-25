package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest newUserRequest);

    List<UserDto> findUsersByRequest(List<Long> userIds, int from, int size);

    void deleteUserById(long catId);

    UserShortDto findUserById(long userId);

    List<UserShortDto> findUsersByIds(List<Long> userIds);
}
