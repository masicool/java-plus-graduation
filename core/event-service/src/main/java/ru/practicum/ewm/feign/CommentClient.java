package ru.practicum.ewm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.api.CommentApi;
import ru.practicum.ewm.api.fallback.CommentFallbackClient;

@FeignClient(name = "comment-service", path = "/internal/comments", fallback = CommentFallbackClient.class)
public interface CommentClient extends CommentApi {
}
