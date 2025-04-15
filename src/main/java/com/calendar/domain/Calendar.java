package com.calendar.domain;

import lombok.*;

import java.util.Objects;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Calendar {
    private Long id;
    private String name;
    private String description;
    private String ownerId;

    public void updateBasicInfo(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Calendar name cannot be empty");
        }
        this.name = name;
        this.description = description;
    }

    public boolean isOwnedBy(String userId) {
        return Objects.equals(this.ownerId, userId);
    }
}