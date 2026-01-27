package org.elkased.eventticketplatform.services;

import org.elkased.eventticketplatform.domain.entities.QrCode;
import org.elkased.eventticketplatform.domain.entities.Ticket;

public interface QrCodeService {

    QrCode generateQrCode(Ticket ticket);
}

