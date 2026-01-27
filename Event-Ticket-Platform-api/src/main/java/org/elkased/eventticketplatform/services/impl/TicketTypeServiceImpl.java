package org.elkased.eventticketplatform.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.*;
import org.elkased.eventticketplatform.exceptions.TicketSoldOutException;
import org.elkased.eventticketplatform.exceptions.TicketTypeNotFoundException;
import org.elkased.eventticketplatform.exceptions.UserNotFoundException;
import org.elkased.eventticketplatform.repositories.TicketRepository;
import org.elkased.eventticketplatform.repositories.TicketTypeRepository;
import org.elkased.eventticketplatform.repositories.UserRepository;
import org.elkased.eventticketplatform.services.QrCodeService;
import org.elkased.eventticketplatform.services.TicketTypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public Ticket purchaseTicket(UUID userId, UUID ticketTypeId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User with id '%s' not found".formatted(userId))
        );

        TicketType ticketType = ticketTypeRepository.findByIdWithLock(ticketTypeId)
                .orElseThrow(() ->
                        new TicketTypeNotFoundException("Ticket type with id '%s' not found".formatted(ticketTypeId))
                );

        int purchasedTicket = ticketRepository.countByTicketTypeId(ticketType.getId());

        if (ticketType.getTotalAvailable() < purchasedTicket + 1) {
            throw new TicketSoldOutException("Ticket type with id '%s' are sold out".formatted(ticketTypeId));
        }

        ticketType.setTotalAvailable(ticketType.getTotalAvailable() - 1);

        Ticket ticket = Ticket.builder()
                .status(TicketStatusEnum.PURCHASED)
                .ticketType(ticketType)
                .purchaser(user)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        QrCode qrCode = qrCodeService.generateQrCode(savedTicket);
        List<QrCode> qrCodes = savedTicket.getQrCodes();
        if (qrCodes == null) {
            savedTicket.setQrCodes(new ArrayList<>());
        }
        savedTicket.getQrCodes().add(qrCode);
        ticketTypeRepository.save(ticketType);

        return savedTicket;
    }
}
