package org.elkased.eventticketplatform.controllers;

import jakarta.transaction.NotSupportedException;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.dtos.TicketValidationRequestDto;
import org.elkased.eventticketplatform.domain.dtos.TicketValidationResponseDto;
import org.elkased.eventticketplatform.domain.entities.TicketValidation;
import org.elkased.eventticketplatform.domain.entities.TicketValidationMethod;
import org.elkased.eventticketplatform.mappers.TicketValidationMapper;
import org.elkased.eventticketplatform.services.TicketValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.cfg.MapperBuilder;

@RestController
@RequestMapping("api/v1/ticket-validations")
@RequiredArgsConstructor
public class TicketValidationController {
    private final TicketValidationService ticketValidationService;
    private final TicketValidationMapper ticketValidationMapper;
    private final MapperBuilder mapperBuilder;

    @PostMapping
    public ResponseEntity<TicketValidationResponseDto> validateTicket(
            @RequestBody TicketValidationRequestDto ticketValidationRequestDto
    ) throws NotSupportedException {
        TicketValidationMethod method = ticketValidationRequestDto.getMethod();
        TicketValidation ticketValidation;
        switch (method) {
            case QR_SCAN ->
                    ticketValidation = ticketValidationService.validateTicketByQrCode(ticketValidationRequestDto.getId());
            case MANUAL ->
                    ticketValidation = ticketValidationService.validateTicketManually(ticketValidationRequestDto.getId());
            case null, default -> throw new NotSupportedException("Not Support this ticket validation method");
        }
        return ResponseEntity.ok(ticketValidationMapper.toTicketValidationResponseDto(ticketValidation));
    }
}
