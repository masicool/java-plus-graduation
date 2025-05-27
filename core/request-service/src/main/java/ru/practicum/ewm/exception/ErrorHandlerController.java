package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exception.type.BadRequestException;
import ru.practicum.ewm.exception.type.ForbiddenException;
import ru.practicum.ewm.exception.type.NotFoundException;
import ru.practicum.ewm.exception.type.RemoteServiceException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
        return buildResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbiddenException(ForbiddenException ex) {
        return buildResponseEntity(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleArgumentNotValidException(MethodArgumentNotValidException ex) {
        return buildResponseEntity(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<ApiError> handleRemoteServiceException(RemoteServiceException ex) {
        return buildResponseEntity(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiError> buildResponseEntity(Exception ex, HttpStatus status) {
        log.error(ex.getMessage());
        return new ResponseEntity<>(
                ApiError.builder()
                        .status(status.toString())
                        .reason(ex.getClass().toString())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build(),
                status
        );
    }
}
