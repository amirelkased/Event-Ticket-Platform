package org.elkased.eventticketplatform.repositories;

import org.elkased.eventticketplatform.domain.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findEventsByOrganizerId(UUID organizerId, Pageable pageable);

    Optional<Event> findEventByIdAndOrganizerId(UUID eventId, UUID organizerId);
}