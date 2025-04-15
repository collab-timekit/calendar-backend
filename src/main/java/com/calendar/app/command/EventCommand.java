package com.calendar.app.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record EventCommand(
    @NotNull(message = "Calendar ID is required")
    Long calendarId,
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot be longer than 100 characters")
    String title,
    
    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    String description,
    
    @NotNull(message = "Start time is required")
    Instant startTime,
    
    @NotNull(message = "End time is required")
    Instant endTime,
    
    @Size(max = 100, message = "Location cannot be longer than 100 characters")
    String location,

    List<AttendeeCommand> attendees
) {}