package com.calendar.app.service;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.app.command.EventCommand;
import com.calendar.app.port.in.NotificationUseCase;
import com.calendar.domain.Attendee;
import com.calendar.domain.Event;
import com.calendar.domain.ResponseStatus;
import com.calendar.domain.exception.AccessDeniedException;
import com.calendar.domain.exception.ConflictException;
import com.calendar.domain.exception.InvalidEventTimeException;
import com.calendar.infra.persistence.repository.AttendeeRepository;
import com.calendar.infra.persistence.repository.CalendarRepository;
import com.calendar.infra.persistence.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AttendeeRepository attendeeRepository;

    @Mock
    private CalendarRepository calendarRepository;

    @Mock
    private NotificationUseCase notificationService;

    private Jwt jwt;

    @InjectMocks
    private EventService eventService;

    private static final String USER_ID = "user123";
    private static final String ORGANIZER_ID = "organizer123";
    private static final Long CALENDAR_ID = 1L;
    private static final Long EVENT_ID = 1L;
    private static final Instant FUTURE_START = Instant.now().plus(1, ChronoUnit.DAYS);
    private static final Instant FUTURE_END = FUTURE_START.plus(1, ChronoUnit.HOURS);

    @BeforeEach
    void setUp() {
        Map<String, Object> claims = Map.of("sub", USER_ID);
        jwt =  new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims
        );
    }

    @Test
    void createEvent_ShouldSuccessfullyCreateEvent() {
        // Arrange
        EventCommand command = new EventCommand(
                CALENDAR_ID, "Test Event", "Description",
                FUTURE_START, FUTURE_END, "Location", null);

        Event expectedEvent = Event.builder()
                .id(EVENT_ID)
                .calendarId(CALENDAR_ID)
                .title("Test Event")
                .organizerId(USER_ID)
                .build();

        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(true);
        when(eventRepository.findConflictingEvents(any(), any(), any())).thenReturn(Collections.emptyList());
        when(eventRepository.save(any())).thenReturn(expectedEvent);

        // Act
        Event result = eventService.createEvent(command, jwt);

        // Assert
        assertNotNull(result);
        assertEquals(EVENT_ID, result.getId());
        verify(calendarRepository).existsByIdAndOwnerId(CALENDAR_ID, USER_ID);
        verify(eventRepository).save(any(Event.class));
        verify(notificationService).scheduleDefaultReminders(any());
    }

    @Test
    void createEvent_ShouldThrowWhenCalendarNotOwned() {
        // Arrange
        EventCommand command = new EventCommand(
                CALENDAR_ID, "Test Event", "Description",
                FUTURE_START, FUTURE_END, "Location", null);

        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> eventService.createEvent(command, jwt));
    }

    @Test
    void createEvent_ShouldThrowWhenTimeConflict() {
        // Arrange
        EventCommand command = new EventCommand(
                CALENDAR_ID, "Test Event", "Description",
                FUTURE_START, FUTURE_END, "Location", null);

        Event conflictingEvent = Event.builder().title("Conflicting Event").build();

        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(true);
        when(eventRepository.findConflictingEvents(any(), any(), any())).thenReturn(List.of(conflictingEvent));

        // Act & Assert
        assertThrows(ConflictException.class, () -> eventService.createEvent(command, jwt));
    }

    @Test
    void createEvent_ShouldThrowWhenInvalidTime() {
        // Arrange
        EventCommand pastCommand = new EventCommand(
                CALENDAR_ID, "Test Event", "Description",
                Instant.now().minus(1, ChronoUnit.DAYS), FUTURE_END, "Location", null);

        EventCommand longCommand = new EventCommand(
                CALENDAR_ID, "Test Event", "Description",
                FUTURE_START, FUTURE_START.plus(8, ChronoUnit.DAYS), "Location", null);

        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(true);

        // Act & Assert
        assertThrows(InvalidEventTimeException.class, () -> eventService.createEvent(pastCommand, jwt));
        assertThrows(InvalidEventTimeException.class, () -> eventService.createEvent(longCommand, jwt));
    }

    @Test
    @Disabled
    void addAttendees_ShouldAddAttendeesSuccessfully() {
        // Arrange
        List<AttendeeCommand> attendees = List.of(
                new AttendeeCommand("attendee1@test.com", false),
                new AttendeeCommand("attendee2@test.com", true)
        );

        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(
                Event.builder().id(EVENT_ID).organizerId(USER_ID).build()
        ));

        // Act
        eventService.addAttendees(EVENT_ID, attendees, jwt);

        // Assert
        verify(attendeeRepository, times(2)).save(any(Attendee.class));
        verify(notificationService).notifyAboutNewAttendees(EVENT_ID, attendees);
    }

    @Test
    @Disabled
    void removeAttendee_ShouldRemoveAttendeeSuccessfully() {
        // Arrange
        Event event = Event.builder().id(EVENT_ID).organizerId(ORGANIZER_ID).build();
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
        when(jwt.getSubject()).thenReturn(ORGANIZER_ID);

        // Act
        eventService.removeAttendee(EVENT_ID, "attendee1", jwt);

        // Assert
        verify(attendeeRepository).deleteByEventIdAndUserId(EVENT_ID, "attendee1");
        verify(notificationService).notifyAboutRemovedAttendee(EVENT_ID, "attendee1");
    }

    @Test
    void removeAttendee_ShouldAllowSelfRemoval() {
        // Arrange
        Event event = Event.builder().id(EVENT_ID).organizerId(USER_ID).build();
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        // Act
        eventService.removeAttendee(EVENT_ID, USER_ID, jwt);

        // Assert
        verify(attendeeRepository).deleteByEventIdAndUserId(EVENT_ID, USER_ID);
        verify(notificationService, never()).notifyAboutRemovedAttendee(any(), any());
    }

    @Test
    void respondToEvent_ShouldUpdateResponseStatus() {
        // Arrange
        Attendee attendee = new Attendee();
        when(attendeeRepository.findByEventIdAndUserId(EVENT_ID, USER_ID))
                .thenReturn(Optional.of(attendee));

        // Act
        eventService.respondToEvent(EVENT_ID, ResponseStatus.ACCEPTED, jwt);

        // Assert
        assertEquals(ResponseStatus.ACCEPTED, attendee.getResponseStatus());
        verify(attendeeRepository).save(attendee);
    }

    @Test
    void getEventDetails_ShouldReturnEventWhenAuthorized() {
        // Arrange
        Event expectedEvent = Event.builder()
                .id(EVENT_ID)
                .organizerId(USER_ID)
                .build();

        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(expectedEvent));

        // Act
        Event result = eventService.getEventDetails(EVENT_ID, jwt);

        // Assert
        assertEquals(expectedEvent, result);
    }

    @Test
    @Disabled
    void getEventDetails_ShouldThrowWhenNotAuthorized() {
        // Arrange
        Event event = Event.builder()
                .id(EVENT_ID)
                .organizerId("otherUser")
                .build();

        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> eventService.getEventDetails(EVENT_ID, jwt));
    }

    @Test
    void validateCalendarOwnership_ShouldThrowWhenNotOwner() {
        // Arrange
        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> eventService.createEvent(
                        new EventCommand(CALENDAR_ID, "Test", "Desc", FUTURE_START, FUTURE_END, "Loc", null),
                        jwt));
    }

    @Test
    @Disabled
    void validateEventTime_ShouldThrowForPastEvent() {
        // Act & Assert
        assertThrows(InvalidEventTimeException.class,
                () -> eventService.createEvent(
                        new EventCommand(CALENDAR_ID, "Test", "Desc", Instant.now().minus(1, ChronoUnit.HOURS), FUTURE_END, "Loc", null),
                        jwt));
    }

    @Test
    void checkForConflicts_ShouldThrowWhenConflictExists() {
        // Arrange
        Event conflictingEvent = Event.builder().title("Conflicting").build();
        when(calendarRepository.existsByIdAndOwnerId(CALENDAR_ID, USER_ID)).thenReturn(true);
        when(eventRepository.findConflictingEvents(any(), any(), any())).thenReturn(List.of(conflictingEvent));

        // Act & Assert
        assertThrows(ConflictException.class,
                () -> eventService.createEvent(
                        new EventCommand(CALENDAR_ID, "Test", "Desc", FUTURE_START, FUTURE_END, "Loc", null),
                        jwt));
    }
}