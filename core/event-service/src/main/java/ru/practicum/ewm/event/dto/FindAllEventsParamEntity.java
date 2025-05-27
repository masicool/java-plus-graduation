package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.dto.event.State;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FindAllEventsParamEntity {
    private List<Long> users;
    private List<State> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int from;
    private int size;
}
