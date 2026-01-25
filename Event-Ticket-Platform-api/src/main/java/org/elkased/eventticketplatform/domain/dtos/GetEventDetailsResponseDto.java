package org.elkased.eventticketplatform.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elkased.eventticketplatform.domain.entities.EventStatusEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetEventDetailsResponseDto {
    private UUID id;
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private String venue;
    private LocalDateTime salesStart;
    private LocalDateTime salesEnd;
    private EventStatusEnum status;
    private List<GetEventTicketTypesResponseDto> ticketTypes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
