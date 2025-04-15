package com.calendar.infra.web.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarRequest {
    private String name;
    private String description;
}
