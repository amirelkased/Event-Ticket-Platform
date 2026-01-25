package org.elkased.eventticketplatform.repositories;

import org.elkased.eventticketplatform.domain.entities.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    Optional<TicketType> findTicketTypeByIdAndEventId(UUID ticketTypeId, UUID eventId);
}