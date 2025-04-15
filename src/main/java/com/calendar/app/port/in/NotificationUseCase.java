package com.calendar.app.port.in;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.domain.Event;
import com.calendar.domain.ResponseStatus;

import java.util.List;

public interface NotificationUseCase {

    void scheduleDefaultReminders(Event event);

    void notifyAboutNewAttendees(Long eventId, List<AttendeeCommand> attendees);

    void notifyAboutRemovedAttendee(Long eventId, String attendeeId);

    void notifyAboutResponse(String organizerId, Long eventId, String userId, ResponseStatus status);  // Odpowiedź uczestnika
}