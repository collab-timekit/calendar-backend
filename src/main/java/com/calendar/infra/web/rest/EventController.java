package com.calendar.infra.web.rest;

import com.calendar.app.command.AttendeeCommand;
import com.calendar.app.command.EventCommand;
import com.calendar.app.port.in.EventUseCase;
import com.calendar.domain.Event;
import com.calendar.domain.ResponseStatus;
import com.calendar.infra.provided.search.model.SearchFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventUseCase eventService;

    @PostMapping("/search")
    public ResponseEntity<List<Event>> getEvents(
            @RequestBody SearchFilter filter,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(eventService.getEvents(filter, jwt));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody EventCommand command,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(eventService.createEvent(command, jwt));
    }

    @PostMapping("/{eventId}/attendees")
    public ResponseEntity<Void> addAttendees(
            @PathVariable Long eventId,
            @RequestBody List<AttendeeCommand> attendees,
            @AuthenticationPrincipal Jwt jwt
    ) {
        eventService.addAttendees(eventId, attendees, jwt);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<Void> removeAttendee(
            @PathVariable Long eventId,
            @PathVariable String attendeeId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        eventService.removeAttendee(eventId, attendeeId, jwt);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/response")
    public ResponseEntity<Void> respondToEvent(
            @PathVariable Long eventId,
            @RequestParam ResponseStatus status,
            @AuthenticationPrincipal Jwt jwt
    ) {
        eventService.respondToEvent(eventId, status, jwt);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventDetails(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(eventService.getEventDetails(eventId, jwt));
    }
}