package ru.practicum.ewm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.api.EventApi;
import ru.practicum.ewm.api.fallback.EventFallbackClient;

@FeignClient(name = "event-service", path = "/internal/events", fallback = EventFallbackClient.class)
public interface EventClient extends EventApi {
}
