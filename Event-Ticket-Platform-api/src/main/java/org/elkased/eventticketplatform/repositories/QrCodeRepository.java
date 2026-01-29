package org.elkased.eventticketplatform.repositories;

import org.elkased.eventticketplatform.domain.entities.QrCode;
import org.elkased.eventticketplatform.domain.entities.QrCodeStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    Optional<QrCode> findQrCodeByTicketIdAndTicketPurchaserId(UUID ticketId, UUID ticketPurchaserId);

    Optional<QrCode> findQrCodeByIdAndStatus(UUID id, QrCodeStatusEnum status);
}