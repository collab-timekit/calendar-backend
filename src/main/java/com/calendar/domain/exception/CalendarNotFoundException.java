package com.calendar.domain.exception;

public class CalendarNotFoundException extends RuntimeException {
    public CalendarNotFoundException(Long calendarId) {
        super("Calendar with id " + calendarId + " not found");
    }
}