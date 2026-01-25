package org.elkased.eventticketplatform.mappers;

import org.elkased.eventticketplatform.domain.CreateEventRequest;
import org.elkased.eventticketplatform.domain.CreateTicketTypeRequest;
import org.elkased.eventticketplatform.domain.UpdateEventRequest;
import org.elkased.eventticketplatform.domain.UpdateTicketTypeRequest;
import org.elkased.eventticketplatform.domain.dtos.*;
import org.elkased.eventticketplatform.domain.entities.Event;
import org.elkased.eventticketplatform.domain.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    CreateTicketTypeRequest fromDto(CreateTicketTypeRequestDto dto);

    CreateEventRequest fromDto(CreateEventRequestDto dto);

    CreateTicketTypeResponseDto toCreateTicketTypeResponseDto(TicketType ticketType);

    CreateEventResponseDto toCreateEventResponseDto(Event event);

    ListEventTicketTypeResponseDto toListEventTicketTypeResponseDto(TicketType ticketType);

    ListEventResponseDto toListEventResponseDto(Event event);

    GetEventTicketTypesResponseDto toGetEventTicketTypesResponseDto(TicketType ticketType);

    GetEventDetailsResponseDto toGetEventDetailsResponseDto(Event event);

    UpdateEventRequest fromDto(UpdateEventRequestDto updateEventRequestDto);

    UpdateTicketTypeRequest fromDto(UpdateTicketTypeRequestDto updateTicketTypeRequestDto);

    UpdateEventResponseDto toUpdateEventResponseDto(Event event);

    UpdateTicketTypeResponseDto toUpdateTicketTypeResponseDto(TicketType ticketType);
}
