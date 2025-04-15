package com.calendar.app.service;

import com.calendar.app.port.in.CalendarUseCase;
import com.calendar.domain.Calendar;
import com.calendar.domain.exception.NotFoundException;
import com.calendar.domain.exception.UnauthorizedAccessException;
import com.calendar.infra.persistence.repository.CalendarRepository;
import com.calendar.infra.web.rest.dto.CalendarRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService implements CalendarUseCase {

    private final CalendarRepository calendarRepository;

    @Override
    @Transactional
    public Calendar createCalendar(CalendarRequest request, String ownerId) {
        var newCalendar = Calendar.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(ownerId)
                .build();
        return calendarRepository.save(newCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public Calendar getCalendar(Long calendarId, String requesterId) {
        Calendar calendar = calendarRepository.findById(calendarId)
                .orElseThrow(() -> new NotFoundException("Calendar with id " + calendarId + " not found"));
        
        if (!calendar.isOwnedBy(requesterId)) {
            throw new UnauthorizedAccessException("User " + requesterId + " cannot access calendar " + calendarId);
        }
        
        return calendar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Calendar> getUserCalendars(String ownerId) {
        return calendarRepository.findAllByOwnerId(ownerId);
    }

    @Override
    @Transactional
    public Calendar updateCalendar(Long calendarId, CalendarRequest request, String ownerId) {
        var calendar = getCalendar(calendarId, ownerId);
        calendar.updateBasicInfo(
                request.getName(),
                request.getDescription());
        return calendarRepository.save(calendar);
    }

    @Override
    @Transactional
    public void deleteCalendar(Long calendarId, String ownerId) {
        Calendar calendar = getCalendar(calendarId, ownerId);
        calendarRepository.deleteById(calendar.getId());
    }
}