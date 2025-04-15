package com.calendar.domain;

import lombok.*;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendee {
    private Long id;
    private Long eventId;
    private String userId;
    private String email;
    private String displayName;
    private ResponseStatus responseStatus;
    private boolean optional;
    private Instant createdAt;
    private Instant updatedAt;

    public void accept() {
        this.responseStatus = ResponseStatus.ACCEPTED;
    }

    public void decline() {
        this.responseStatus = ResponseStatus.DECLINED;
    }

    public void markAsTentative() {
        this.responseStatus = ResponseStatus.TENTATIVE;
    }

    public boolean hasResponded() {
        return responseStatus != ResponseStatus.PENDING;
    }
}