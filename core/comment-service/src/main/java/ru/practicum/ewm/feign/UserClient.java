package ru.practicum.ewm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.api.UserApi;
import ru.practicum.ewm.api.fallback.UserFallbackClient;

@FeignClient(name = "user-service", path = "/internal/users", fallback = UserFallbackClient.class)
public interface UserClient extends UserApi {
}
