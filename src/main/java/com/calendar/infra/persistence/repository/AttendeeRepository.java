package com.calendar.infra.persistence.repository;

import com.calendar.domain.Attendee;
import com.calendar.infra.persistence.entity.AttendeeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.calendar.infra.persistence.mapper.AttendeeMapper.attendeeMapper;

@Repository
@RequiredArgsConstructor
public class AttendeeRepository {

    private final JpaAttendeeRepository jpaRepository;

    public Attendee save(Attendee attendee) {
        AttendeeEntity entity = attendeeMapper.toEntity(attendee);
        AttendeeEntity savedEntity = jpaRepository.save(entity);
        return attendeeMapper.toDomain(savedEntity);
    }

    public List<Attendee> findByEventId(Long eventId) {
        return jpaRepository.findByEventId(eventId).stream()
                .map(attendeeMapper::toDomain)
                .toList();
    }

    public Optional<Attendee> findByEventIdAndUserId(Long eventId, String userId) {
        return jpaRepository.findByEventIdAndUserId(eventId, userId)
                .map(attendeeMapper::toDomain);
    }

    public void deleteByEventIdAndUserId(Long eventId, String attendeeId) {
        jpaRepository.deleteByEventIdAndUserId(eventId, attendeeId);
    }

    public interface JpaAttendeeRepository extends JpaRepository<AttendeeEntity, Long> {
        List<AttendeeEntity> findByEventId(Long eventId);
        Optional<AttendeeEntity> findByEventIdAndUserId(Long eventId, String userId);
        void deleteByEventIdAndUserId(Long eventId, String userId);
    }
}