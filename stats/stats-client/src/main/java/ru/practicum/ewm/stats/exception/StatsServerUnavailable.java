package ru.practicum.ewm.stats.exception;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message) {
        super(message);
    }
}
