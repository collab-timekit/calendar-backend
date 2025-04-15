package com.calendar.app.service;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.app.port.in.NotificationUseCase;
import com.calendar.domain.Event;
import com.calendar.domain.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService implements NotificationUseCase {
    @Override
    public void scheduleDefaultReminders(Event event) {
        log.info("Zaplanowano przypomnienia dla wydarzenia: {}", event.getTitle());
    }

    @Override
    public void notifyAboutNewAttendees(Long eventId, List<AttendeeCommand> attendees) {
        log.info("Nowi uczestnicy dodani do wydarzenia {}: {}", eventId, attendees);
    }

    @Override
    public void notifyAboutRemovedAttendee(Long eventId, String attendeeId) {
        log.info("Uczestnik {} został usunięty z wydarzenia {}", attendeeId, eventId);
    }

    @Override
    public void notifyAboutResponse(String organizerId, Long eventId, String userId, ResponseStatus status) {
        log.info("Użytkownik {} zmienił status odpowiedzi na {} w wydarzeniu {}", userId, status, eventId);
    }
}