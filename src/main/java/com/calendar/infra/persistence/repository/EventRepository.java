package com.calendar.infra.persistence.repository;

import com.calendar.domain.Event;
import com.calendar.domain.exception.CalendarNotFoundException;
import com.calendar.infra.persistence.entity.EventEntity;
import com.calendar.infra.provided.search.model.SearchFilter;
import com.calendar.infra.provided.search.spec.SearchSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.time.Instant;

import static com.calendar.infra.persistence.mapper.EventMapper.eventMapper;

@Repository
@RequiredArgsConstructor
public class EventRepository  {

    private final JpaEventRepository jpaRepository;
    private final CalendarRepository calendarRepository;

    public Event save(Event event) {
        EventEntity entity = eventMapper.toEntity(event);

        calendarRepository.findById(event.getCalendarId())
                .orElseThrow(() -> new CalendarNotFoundException(event.getCalendarId()));

        EventEntity savedEntity = jpaRepository.save(entity);
        return eventMapper.toDomain(savedEntity);
    }

    public Optional<Event> findById(Long eventId) {
        return jpaRepository.findById(eventId)
                .map(eventMapper::toDomain);
    }

    public List<Event> findConflictingEvents(Long calendarId, Instant start, Instant end) {
        return jpaRepository.findConflictingEvents(calendarId, start, end).stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    public List<Event> findAll(SearchFilter filter) {
        var specificationBuilder = new SearchSpecificationBuilder<EventEntity>(filter);
        var specification = specificationBuilder.buildSpecification();
        var pageRequest = specificationBuilder.buildPageRequest();
        return jpaRepository.findAll(specification, pageRequest).stream()
                .map(eventMapper::toDomain)
                .toList();
    }

    public interface JpaEventRepository extends JpaRepository<EventEntity, Long>,
            JpaSpecificationExecutor<EventEntity> {

        @Query("SELECT e FROM EventEntity e WHERE e.calendar.id = :calendarId " +
                "AND ((e.startTime BETWEEN :start AND :end) OR " +
                "(e.endTime BETWEEN :start AND :end) OR " +
                "(e.startTime <= :start AND e.endTime >= :end)) " +
                "AND e.status <> 'CANCELLED'")
        List<EventEntity> findConflictingEvents(
                @Param("calendarId") Long calendarId,
                @Param("start") Instant start,
                @Param("end") Instant end);
    }
}