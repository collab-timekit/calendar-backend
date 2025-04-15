package com.calendar.app.port.in;

import com.calendar.domain.Calendar;
import com.calendar.infra.web.rest.dto.CalendarRequest;

import java.util.List;

public interface CalendarUseCase {
    Calendar createCalendar(CalendarRequest request, String ownerId);
    Calendar getCalendar(Long calendarId, String requesterId);
    List<Calendar> getUserCalendars(String ownerId);
    Calendar updateCalendar(Long calendarId, CalendarRequest request, String ownerId);
    void deleteCalendar(Long calendarId, String ownerId);
}