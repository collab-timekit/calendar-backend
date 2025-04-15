package com.calendar.app.port.in;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.app.command.EventCommand;
import com.calendar.domain.Event;
import com.calendar.domain.ResponseStatus;
import com.calendar.infra.provided.search.model.SearchFilter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;

public interface EventUseCase {
    Event createEvent(EventCommand command, @AuthenticationPrincipal Jwt jwt);
    void addAttendees(Long eventId, List<AttendeeCommand> attendees, @AuthenticationPrincipal Jwt jwt);
    void removeAttendee(Long eventId, String attendeeId, @AuthenticationPrincipal Jwt jwt);
    void respondToEvent(Long eventId, ResponseStatus status, @AuthenticationPrincipal Jwt jwt);
    Event getEventDetails(Long eventId, @AuthenticationPrincipal Jwt jwt);

    List<Event> getEvents(SearchFilter filter, Jwt jwt);
}