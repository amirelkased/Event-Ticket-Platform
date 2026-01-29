package org.elkased.eventticketplatform.controllers;

import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.dtos.GetTicketResponseDto;
import org.elkased.eventticketplatform.domain.dtos.ListTicketResponseDto;
import org.elkased.eventticketplatform.domain.entities.Ticket;
import org.elkased.eventticketplatform.mappers.TicketMapper;
import org.elkased.eventticketplatform.services.QrCodeService;
import org.elkased.eventticketplatform.services.TicketService;
import org.elkased.eventticketplatform.util.JwtUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService ticketService;
    private final QrCodeService qrCodeService;
    private final TicketMapper ticketMapper;

    @GetMapping
    public ResponseEntity<Page<ListTicketResponseDto>> listTickets(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID user = JwtUtil.parseUserId(jwt);
        Page<Ticket> tickets = ticketService.listTicketsForUser(user, pageable);
        return ResponseEntity.ok(tickets.map(ticketMapper::toListTicketResponseDto));
    }

    @GetMapping("{ticketId}")
    public ResponseEntity<GetTicketResponseDto> getTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        UUID user = JwtUtil.parseUserId(jwt);
        return ticketService.getTicketForUser(user, ticketId)
                .map(ticketMapper::toGetTicketResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("{ticketId}/qr-codes")
    public ResponseEntity<byte[]> getTicketQrCode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketId
    ) {
        UUID user = JwtUtil.parseUserId(jwt);
        byte[] qrCodeImage = qrCodeService.getQrCodeImageForUserAndTicket(user, ticketId);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);
        httpHeaders.setContentLength(qrCodeImage.length);

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(qrCodeImage);
    }
}
