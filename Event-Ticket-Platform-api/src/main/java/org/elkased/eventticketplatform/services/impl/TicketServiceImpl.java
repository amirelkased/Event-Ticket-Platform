package org.elkased.eventticketplatform.services.impl;

import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.Ticket;
import org.elkased.eventticketplatform.repositories.TicketRepository;
import org.elkased.eventticketplatform.services.TicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    @Override
    public Page<Ticket> listTicketsForUser(UUID userId, Pageable pageable) {
        return ticketRepository.findTicketsByPurchaserId(userId, pageable);
    }

    @Override
    public Optional<Ticket> getTicketForUser(UUID userId, UUID ticketId) {
        return ticketRepository.findTicketByIdAndPurchaserId(ticketId, userId);
    }
}
