package org.elkased.eventticketplatform.services;

import org.elkased.eventticketplatform.domain.entities.QrCode;
import org.elkased.eventticketplatform.domain.entities.Ticket;

import java.util.UUID;

public interface QrCodeService {
    QrCode generateQrCode(Ticket ticket);

    byte[] getQrCodeImageForUserAndTicket(UUID userId, UUID ticketId);
}

