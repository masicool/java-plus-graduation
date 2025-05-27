package ru.practicum.ewm.api;

import feign.FeignException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.util.List;

public interface UserApi {
    @GetMapping("/{userId}")
    UserShortDto findById(@PathVariable long userId) throws FeignException;

    @GetMapping
    List<UserShortDto> findByIdIn(@RequestParam List<Long> userIds) throws FeignException;
}
