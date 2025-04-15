package com.calendar.infra.persistence.mapper;

import com.calendar.domain.Attendee;
import com.calendar.infra.persistence.entity.AttendeeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AttendeeMapper {
    AttendeeMapper attendeeMapper = Mappers.getMapper(AttendeeMapper.class);

    @Mapping(target = "eventId", source = "event.id")
    Attendee toDomain(AttendeeEntity entity);

    @Mapping(target = "event", ignore = true)
    AttendeeEntity toEntity(Attendee domain);
}