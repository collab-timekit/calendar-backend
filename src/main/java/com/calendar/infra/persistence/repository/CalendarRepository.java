package com.calendar.infra.persistence.repository;

import com.calendar.domain.Calendar;
import com.calendar.infra.persistence.entity.CalendarEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.calendar.infra.persistence.mapper.CalendarMapper.calendarMapper;


@Repository
@RequiredArgsConstructor
public class CalendarRepository {

    private final JpaCalendarRepository repository;

    public Calendar save(Calendar calendar) {
        CalendarEntity entity = calendarMapper.toEntity(calendar);
        CalendarEntity savedEntity = repository.save(entity);
        return calendarMapper.toDomain(savedEntity);
    }

    public Optional<Calendar> findById(Long calendarId) {
        return repository.findById(calendarId)
                .map(calendarMapper::toDomain);
    }

    public List<Calendar> findAllByOwnerId(String ownerId) {
        return repository.findAllByOwnerId(ownerId).stream()
                .map(calendarMapper::toDomain)
                .toList();
    }

    public void deleteById(Long calendarId) {
        repository.deleteById(calendarId);
    }

    public boolean existsByIdAndOwnerId(Long calendarId, String userId) {
        return repository.existsByIdAndOwnerId(calendarId, userId);
    }

    public interface JpaCalendarRepository extends JpaRepository<CalendarEntity, Long>,
            JpaSpecificationExecutor<CalendarEntity> {
        List<CalendarEntity> findAllByOwnerId(String ownerId);
        boolean existsByIdAndOwnerId(Long calendarId, String userId);
    }
}
