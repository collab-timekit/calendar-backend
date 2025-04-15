package com.calendar.app.service;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.app.command.EventCommand;
import com.calendar.app.port.in.EventUseCase;
import com.calendar.app.port.in.NotificationUseCase;
import com.calendar.domain.Attendee;
import com.calendar.domain.Event;
import com.calendar.domain.EventStatus;
import com.calendar.domain.ResponseStatus;
import com.calendar.domain.exception.*;
import com.calendar.infra.persistence.repository.AttendeeRepository;
import com.calendar.infra.persistence.repository.CalendarRepository;
import com.calendar.infra.persistence.repository.EventRepository;
import com.calendar.infra.provided.search.model.SearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService implements EventUseCase {

    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;
    private final CalendarRepository calendarRepository;
    private final NotificationUseCase notificationService;

    @Override
    @Transactional
    public Event createEvent(EventCommand command, @AuthenticationPrincipal Jwt jwt) {
        String organizerId = jwt.getSubject();
        validateCalendarOwnership(command.calendarId(), organizerId);
        validateEventTime(command.startTime(), command.endTime());
        checkForConflicts(command.calendarId(), command.startTime(), command.endTime());

        Event event = Event.builder()
                .calendarId(command.calendarId())
                .title(command.title())
                .description(command.description())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .location(command.location())
                .organizerId(organizerId)
                .status(EventStatus.CONFIRMED)
                .build();

        Event savedEvent = eventRepository.save(event);

        if (command.attendees() != null && !command.attendees().isEmpty()) {
            addAttendeesInternal(savedEvent.getId(), command.attendees(), jwt);
        }

        notificationService.scheduleDefaultReminders(savedEvent);
        return savedEvent;
    }

    private void addAttendeesInternal(Long eventId, List<AttendeeCommand> attendees, Jwt jwt) {
        attendees.forEach(attendeeCmd -> {
            String email = attendeeCmd.email();
            String userId = getUserIdFromEmail(email);

            Map<String, Object> claims = jwt.getClaims();
            String displayName = (String) claims.getOrDefault("name", email);

            Attendee attendee = Attendee.builder()
                    .eventId(eventId)
                    .userId(userId)
                    .email(email)
                    .displayName(displayName)
                    .responseStatus(ResponseStatus.PENDING)
                    .optional(attendeeCmd.optional())
                    .build();

            attendeeRepository.save(attendee);
        });
    }

    @Override
    @Transactional
    public void addAttendees(Long eventId, List<AttendeeCommand> attendees, @AuthenticationPrincipal Jwt jwt) {
        addAttendeesInternal(eventId, attendees, jwt);
        notificationService.notifyAboutNewAttendees(eventId, attendees);
    }

    @Override
    @Transactional
    public void removeAttendee(Long eventId, String attendeeId, @AuthenticationPrincipal Jwt jwt) {
        String requesterId = jwt.getSubject();
        Event event = getEventWithAuthorization(eventId, requesterId);

        if (!requesterId.equals(attendeeId)) {
            validateOrganizer(event, requesterId);
        }

        attendeeRepository.deleteByEventIdAndUserId(eventId, attendeeId);

        if (!event.getOrganizerId().equals(attendeeId)) {
            notificationService.notifyAboutRemovedAttendee(eventId, attendeeId);
        }
    }

    @Override
    @Transactional
    public void respondToEvent(Long eventId, ResponseStatus status, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        Attendee attendee = attendeeRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Attendee for event with id " + eventId + " not found"));

        switch (status) {
            case ACCEPTED -> attendee.accept();
            case DECLINED -> attendee.decline();
            case TENTATIVE -> attendee.markAsTentative();
        }

        attendeeRepository.save(attendee);
    }

    @Override
    public Event getEventDetails(Long eventId, Jwt jwt) {
        String userId = jwt.getSubject();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        if (!event.isVisibleTo(userId)) {
            throw new AccessDeniedException("User " + userId + " has no access to this event.");
        }

        return event;
    }

    @Override
    public List<Event> getEvents(SearchFilter filter, Jwt jwt) {
        return eventRepository.findAll(filter)
                .stream()
                .filter(event -> event.isVisibleTo(jwt.getSubject()))
                .collect(Collectors.toList());
    }

    private String getUserIdFromEmail(String email) {
        return email;
    }

    private void validateCalendarOwnership(Long calendarId, String userId) {
        if (!calendarRepository.existsByIdAndOwnerId(calendarId, userId)) {
            throw new AccessDeniedException(
                    "User " + userId + " is not owner of calendar " + calendarId);
        }
    }

    private void validateEventTime(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidEventTimeException("Start and end time must be specified");
        }

        if (startTime.isBefore(Instant.now())) {
            throw new InvalidEventTimeException("Cannot create event in the past");
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidEventTimeException("End time must be after start time");
        }

        if (Duration.between(startTime, endTime).toDays() > 7) {
            throw new InvalidEventTimeException("Event cannot last longer than 7 days");
        }
    }

    private void checkForConflicts(Long calendarId, Instant startTime, Instant endTime) {
        List<Event> conflictingEvents = eventRepository.findConflictingEvents(
                calendarId,
                startTime.minus(1, ChronoUnit.HOURS),
                endTime.plus(1, ChronoUnit.HOURS)
        );

        if (!conflictingEvents.isEmpty()) {
            String conflictingTitles = conflictingEvents.stream()
                    .map(Event::getTitle)
                    .collect(Collectors.joining(", "));

            throw new ConflictException(
                    "Time conflict with existing events: " + conflictingTitles);
        }
    }

    private Event getEventWithAuthorization(Long eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        if (!event.getOrganizerId().equals(userId)) {
            throw new UnauthorizedAccessException("User is not the organizer of this event.");
        }
        return event;
    }

    private void validateOrganizer(Event event, String userId) {
        if (!event.getOrganizerId().equals(userId)) {
            throw new AccessDeniedException("User " + userId + " is not the organizer of this event.");
        }
    }
}