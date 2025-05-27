package ru.practicum.ewm.event.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.model.AdminStateAction;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequestDto extends UpdateEventBaseRequestDto {
    AdminStateAction stateAction;
}
