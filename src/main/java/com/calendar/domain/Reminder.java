package com.calendar.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reminder {
    private UUID id;
    private Event event;
    private LocalDateTime reminderTime;
    private ReminderMethod method;
}