package com.calendar.infra.web.rest;

import com.calendar.app.port.in.CalendarUseCase;
import com.calendar.domain.Calendar;
import com.calendar.infra.web.rest.dto.CalendarRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendars")
@RequiredArgsConstructor
@Validated
public class CalendarController {

    private final CalendarUseCase calendarUseCase;

    @PostMapping
    public ResponseEntity<Calendar> createCalendar(
            @RequestBody @Valid CalendarRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calendarUseCase.createCalendar(
                        request,
                        jwt.getSubject()));
    }

    @GetMapping("/{calendarId}")
    public ResponseEntity<Calendar> getCalendar(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                calendarUseCase.getCalendar(calendarId, jwt.getSubject()));
    }

    @GetMapping
    public ResponseEntity<List<Calendar>> getUserCalendars(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                calendarUseCase.getUserCalendars(jwt.getSubject()));
    }

    @PutMapping("/{calendarId}")
    public ResponseEntity<Calendar> updateCalendar(
            @PathVariable Long calendarId,
            @RequestBody @Valid CalendarRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                calendarUseCase.updateCalendar(
                        calendarId,
                        request,
                        jwt.getSubject()));
    }

    @DeleteMapping("/{calendarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCalendar(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal Jwt jwt) {
        calendarUseCase.deleteCalendar(calendarId, jwt.getSubject());
    }
}