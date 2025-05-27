package ru.practicum.ewm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.api.RequestApi;
import ru.practicum.ewm.api.fallback.RequestFallbackClient;

@FeignClient(name = "request-service", path = "/internal/requests", fallback = RequestFallbackClient.class)
public interface RequestClient extends RequestApi {
}
