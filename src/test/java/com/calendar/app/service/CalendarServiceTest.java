package com.calendar.app.service;

import static org.junit.jupiter.api.Assertions.*;

import com.calendar.domain.Calendar;
import com.calendar.domain.exception.NotFoundException;
import com.calendar.domain.exception.UnauthorizedAccessException;
import com.calendar.infra.persistence.repository.CalendarRepository;
import com.calendar.infra.web.rest.dto.CalendarRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private CalendarService calendarService;

    private CalendarRequest validRequest;
    private Calendar sampleCalendar;
    private final String OWNER_ID = "user123";
    private final String OTHER_USER_ID = "user456";
    private final Long CALENDAR_ID = 1L;

    @BeforeEach
    void setUp() {
        validRequest = new CalendarRequest("Test Calendar", "Test Description");
        sampleCalendar = Calendar.builder()
                .id(CALENDAR_ID)
                .name("Test Calendar")
                .description("Test Description")
                .ownerId(OWNER_ID)
                .build();
    }

    @Test
    void createCalendar_ShouldSaveAndReturnNewCalendar() {
        // Given
        when(calendarRepository.save(any(Calendar.class))).thenReturn(sampleCalendar);

        // When
        Calendar result = calendarService.createCalendar(validRequest, OWNER_ID);

        // Then
        assertNotNull(result);
        assertEquals(validRequest.getName(), result.getName());
        assertEquals(validRequest.getDescription(), result.getDescription());
        assertEquals(OWNER_ID, result.getOwnerId());
        verify(calendarRepository, times(1)).save(any(Calendar.class));
    }

    @Test
    void getCalendar_ShouldReturnCalendar_WhenUserIsOwner() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));

        // When
        Calendar result = calendarService.getCalendar(CALENDAR_ID, OWNER_ID);

        // Then
        assertNotNull(result);
        assertEquals(CALENDAR_ID, result.getId());
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
    }

    @Test
    void getCalendar_ShouldThrowNotFoundException_WhenCalendarDoesNotExist() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> calendarService.getCalendar(CALENDAR_ID, OWNER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
    }

    @Test
    void getCalendar_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwner() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> calendarService.getCalendar(CALENDAR_ID, OTHER_USER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
    }

    @Test
    void getUserCalendars_ShouldReturnListOfCalendars() {
        // Given
        List<Calendar> expectedCalendars = List.of(sampleCalendar);
        when(calendarRepository.findAllByOwnerId(OWNER_ID)).thenReturn(expectedCalendars);

        // When
        List<Calendar> result = calendarService.getUserCalendars(OWNER_ID);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleCalendar, result.getFirst());
        verify(calendarRepository, times(1)).findAllByOwnerId(OWNER_ID);
    }

    @Test
    void updateCalendar_ShouldUpdateAndReturnCalendar_WhenUserIsOwner() {
        // Given
        CalendarRequest updateRequest = new CalendarRequest("Updated Name", "Updated Description");
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));
        when(calendarRepository.save(any(Calendar.class))).thenReturn(sampleCalendar);

        // When
        Calendar result = calendarService.updateCalendar(CALENDAR_ID, updateRequest, OWNER_ID);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getName(), result.getName());
        assertEquals(updateRequest.getDescription(), result.getDescription());
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, times(1)).save(sampleCalendar);
    }

    @Test
    void updateCalendar_ShouldThrowNotFoundException_WhenCalendarDoesNotExist() {
        // Given
        CalendarRequest updateRequest = new CalendarRequest("Updated Name", "Updated Description");
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> calendarService.updateCalendar(CALENDAR_ID, updateRequest, OWNER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, never()).save(any());
    }

    @Test
    void updateCalendar_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwner() {
        // Given
        CalendarRequest updateRequest = new CalendarRequest("Updated Name", "Updated Description");
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> calendarService.updateCalendar(CALENDAR_ID, updateRequest, OTHER_USER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, never()).save(any());
    }

    @Test
    void deleteCalendar_ShouldDeleteCalendar_WhenUserIsOwner() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));
        doNothing().when(calendarRepository).deleteById(CALENDAR_ID);

        // When
        calendarService.deleteCalendar(CALENDAR_ID, OWNER_ID);

        // Then
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, times(1)).deleteById(CALENDAR_ID);
    }

    @Test
    void deleteCalendar_ShouldThrowNotFoundException_WhenCalendarDoesNotExist() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> calendarService.deleteCalendar(CALENDAR_ID, OWNER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, never()).deleteById(any());
    }

    @Test
    void deleteCalendar_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwner() {
        // Given
        when(calendarRepository.findById(CALENDAR_ID)).thenReturn(Optional.of(sampleCalendar));

        // When & Then
        assertThrows(UnauthorizedAccessException.class,
                () -> calendarService.deleteCalendar(CALENDAR_ID, OTHER_USER_ID));
        verify(calendarRepository, times(1)).findById(CALENDAR_ID);
        verify(calendarRepository, never()).deleteById(any());
    }
}