package com.calendar.infra.persistence.mapper;

import com.calendar.domain.Calendar;
import com.calendar.infra.persistence.entity.CalendarEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;

@Mapper
public interface CalendarMapper {
    CalendarMapper calendarMapper = Mappers.getMapper(CalendarMapper.class);

    @Mapping(target = "events", ignore = true)
    CalendarEntity toEntity(Calendar domain);

    Calendar toDomain(CalendarEntity entity);
}