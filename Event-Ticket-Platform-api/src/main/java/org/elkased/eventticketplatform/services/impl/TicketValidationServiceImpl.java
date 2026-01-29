package org.elkased.eventticketplatform.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.*;
import org.elkased.eventticketplatform.exceptions.QrCodeNotFoundException;
import org.elkased.eventticketplatform.exceptions.TicketNotFoundException;
import org.elkased.eventticketplatform.exceptions.TicketTypeNotFoundException;
import org.elkased.eventticketplatform.repositories.QrCodeRepository;
import org.elkased.eventticketplatform.repositories.TicketRepository;
import org.elkased.eventticketplatform.repositories.TicketValidationRepository;
import org.elkased.eventticketplatform.services.TicketValidationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketValidationServiceImpl implements TicketValidationService {
    private final TicketValidationRepository ticketValidationRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;

    @Override
    public TicketValidation validateTicketByQrCode(UUID qrCodeId) {
        QrCode qrCode = qrCodeRepository.findQrCodeByIdAndStatus(qrCodeId, QrCodeStatusEnum.ACTIVE)
                .orElseThrow(() -> new QrCodeNotFoundException(
                        String.format(
                                "QR Code with ID %s was not found", qrCodeId
                        )
                ));

        return validateTicket(qrCode.getTicket(), TicketValidationMethod.QR_SCAN);
    }

    private TicketValidation validateTicket(Ticket ticket, TicketValidationMethod ticketValidationMethod) {
        TicketValidation ticketValidation = TicketValidation.builder()
                .validationMethod(ticketValidationMethod)
                .ticket(ticket)
                .build();
        TicketValidationStatusEnum ticketValidationStatusEnum = ticket.getTicketValidations()
                .stream()
                .filter(v -> TicketValidationStatusEnum.VALID.equals(v.getStatus()))
                .findFirst()
                .map(v -> TicketValidationStatusEnum.INVALID)
                .orElse(TicketValidationStatusEnum.VALID);
        ticketValidation.setStatus(ticketValidationStatusEnum);
        return ticketValidationRepository.save(ticketValidation);
    }

    @Override
    public TicketValidation validateTicketManually(UUID ticketId) {
        return validateTicket(ticketRepository.findById(ticketId)
                        .orElseThrow(TicketNotFoundException::new),
                TicketValidationMethod.MANUAL);
    }
}
