package org.elkased.eventticketplatform.services;

import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.TicketValidation;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface TicketValidationService {

    TicketValidation validateTicketByQrCode(UUID qrCodeId);

    TicketValidation validateTicketManually(UUID ticketId);

}

