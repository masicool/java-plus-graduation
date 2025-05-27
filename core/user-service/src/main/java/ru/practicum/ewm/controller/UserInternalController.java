package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.api.UserApi;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.service.UserService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/internal/users")
@RestController
public class UserInternalController implements UserApi {
    private final UserService userService;

    @Override
    public UserShortDto findById(long userId) {
        return userService.findUserById(userId);
    }

    @Override
    public List<UserShortDto> findByIdIn(List<Long> userIds) {
        return userService.findUsersByIds(userIds);
    }
}
