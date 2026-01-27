package org.elkased.eventticketplatform.controllers;

import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.services.TicketTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.elkased.eventticketplatform.util.JwtUtil.parseUserId;

@RestController
@RequestMapping(value = "api/v1/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {
    private final TicketTypeService ticketTypeService;

    @PostMapping(value = "{ticketTypeId}/tickets")
    public ResponseEntity<Void> purchaseTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ticketTypeId
    ) {
        UUID user = parseUserId(jwt);
        ticketTypeService.purchaseTicket(user, ticketTypeId);
        return ResponseEntity.noContent().build();
    }
}

