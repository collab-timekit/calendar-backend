package com.calendar.app.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AttendeeCommand(
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    boolean optional
) {}