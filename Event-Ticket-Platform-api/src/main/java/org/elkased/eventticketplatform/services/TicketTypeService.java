package org.elkased.eventticketplatform.services;

import org.elkased.eventticketplatform.domain.entities.Ticket;

import java.util.UUID;

public interface TicketTypeService {
    Ticket purchaseTicket(UUID userId, UUID ticketTypeId);
}

