package com.calendar.domain;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    private Long id;
    private Long calendarId;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private String organizerId;
    private EventStatus status;
    private List<Attendee> attendees;

    public boolean isVisibleTo(String userId) {
        return this.organizerId.equals(userId) ||
                attendees.stream().anyMatch(a -> a.getUserId().equals(userId));
    }

    public boolean canBeModifiedBy(String userId) {
        return this.organizerId.equals(userId);
    }

    public void addAttendee(Attendee attendee) {
        if (attendees.stream().noneMatch(a -> a.getUserId().equals(attendee.getUserId()))) {
            attendees.add(attendee);
        }
    }

    public void removeAttendee(String userId) {
        attendees.removeIf(a -> a.getUserId().equals(userId));
    }
}