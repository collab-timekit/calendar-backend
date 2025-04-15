package com.calendar.infra.persistence.mapper;

import com.calendar.domain.Event;
import com.calendar.infra.persistence.entity.EventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AttendeeMapper.class})
public interface EventMapper {
    EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "attendees", source = "attendeeEntities")
    Event toDomain(EventEntity entity);

    @Mapping(target = "attendeeEntities", source = "attendees")
    EventEntity toEntity(Event domain);
}