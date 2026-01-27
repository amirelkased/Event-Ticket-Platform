package org.elkased.eventticketplatform.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.CreateEventRequest;
import org.elkased.eventticketplatform.domain.UpdateEventRequest;
import org.elkased.eventticketplatform.domain.UpdateTicketTypeRequest;
import org.elkased.eventticketplatform.domain.entities.Event;
import org.elkased.eventticketplatform.domain.entities.EventStatusEnum;
import org.elkased.eventticketplatform.domain.entities.TicketType;
import org.elkased.eventticketplatform.domain.entities.User;
import org.elkased.eventticketplatform.exceptions.EventNotFoundException;
import org.elkased.eventticketplatform.exceptions.EventUpdateException;
import org.elkased.eventticketplatform.exceptions.TicketTypeNotFoundException;
import org.elkased.eventticketplatform.exceptions.UserNotFoundException;
import org.elkased.eventticketplatform.repositories.EventRepository;
import org.elkased.eventticketplatform.repositories.UserRepository;
import org.elkased.eventticketplatform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public Event createEvent(UUID organizerId, CreateEventRequest event) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() ->
                        new UserNotFoundException(String.format("User with id '%s' not found", organizerId))
                );
        Event eventToCreate = new Event();
        List<TicketType> ticketTypes = event.getTicketTypes().stream()
                .map(ticket -> TicketType.builder()
                        .name(ticket.getName())
                        .description(ticket.getDescription())
                        .totalAvailable(ticket.getTotalAvailable())
                        .price(ticket.getPrice())
                        .event(eventToCreate)
                        .build())
                .toList();

        eventToCreate.setName(event.getName());
        eventToCreate.setStart(event.getStart());
        eventToCreate.setEnd(event.getEnd());
        eventToCreate.setSalesStart(event.getSalesStart());
        eventToCreate.setSalesEnd(event.getSalesEnd());
        eventToCreate.setVenue(event.getVenue());
        eventToCreate.setStatus(event.getStatus());
        eventToCreate.setOrganizer(organizer);
        eventToCreate.setTicketTypes(ticketTypes);

        return eventRepository.save(eventToCreate);
    }

    @Override
    public Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable) {
        return eventRepository.findEventsByOrganizerId(organizerId, pageable);
    }

    @Override
    public Optional<Event> getEventForOrganizer(UUID eventId, UUID organizerId) {
        return eventRepository.findEventByIdAndOrganizerId(eventId, organizerId);
    }

    @Override
    @Transactional
    public Event updateEventForOrganizer(UUID organizerId, UUID eventId, UpdateEventRequest updateEventRequest) {
        if (updateEventRequest.getId() == null) {
            throw new EventNotFoundException("Event Id cannot be null");
        }

        if (!eventId.equals(updateEventRequest.getId())) {
            throw new EventUpdateException("Cannot update the id of an Event");
        }

        Event existingEvent = getEventForOrganizer(eventId, organizerId)
                .orElseThrow(
                        () -> new EventNotFoundException(String.format("There is no Event with id '%s'", eventId))
                );

        existingEvent.setName(updateEventRequest.getName());
        existingEvent.setStart(updateEventRequest.getStart());
        existingEvent.setEnd(updateEventRequest.getEnd());
        existingEvent.setVenue(updateEventRequest.getVenue());
        existingEvent.setSalesStart(updateEventRequest.getSalesStart());
        existingEvent.setSalesEnd(updateEventRequest.getSalesEnd());
        existingEvent.setStatus(updateEventRequest.getStatus());

        migrateEventTicketTypes(existingEvent, updateEventRequest.getTicketTypes());

        return eventRepository.save(existingEvent);
    }

    private void migrateEventTicketTypes(Event existingEvent, List<UpdateTicketTypeRequest> updateTicketTypes) {
        Set<UUID> uuids = updateTicketTypes.stream()
                .map(UpdateTicketTypeRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingEvent.getTicketTypes()
                .removeIf(ticketType ->
                        !uuids.contains(ticketType.getId()));

        Map<UUID, TicketType> ticketTypeMap = existingEvent.getTicketTypes()
                .stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (UpdateTicketTypeRequest updateTicketType : updateTicketTypes) {
            if (updateTicketType.getId() == null) {
                existingEvent.getTicketTypes()
                        .add(
                                TicketType.builder()
                                        .name(updateTicketType.getName())
                                        .price(updateTicketType.getPrice())
                                        .description(updateTicketType.getDescription())
                                        .totalAvailable(updateTicketType.getTotalAvailable())
                                        .event(existingEvent)
                                        .build()
                        );
            } else if (ticketTypeMap.containsKey(updateTicketType.getId())) {
                TicketType ticketType = ticketTypeMap.get(updateTicketType.getId());
                ticketType.setName(updateTicketType.getName());
                ticketType.setDescription(updateTicketType.getDescription());
                ticketType.setPrice(updateTicketType.getPrice());
            } else {
                throw new TicketTypeNotFoundException(
                        String.format("TicketType with id '%s' does not exist", updateTicketType.getId())
                );
            }
        }
    }

    @Override
    @Transactional
    public void deleteEvent(UUID organizerId, UUID eventId) {
        getEventForOrganizer(eventId, organizerId).ifPresent(eventRepository::delete);
    }

    @Override
    public Page<Event> listPublishedEvents(Pageable pageable) {
        return eventRepository.findEventsByStatus(EventStatusEnum.PUBLISHED, pageable);
    }

    @Override
    public Page<Event> searchPublishedEvents(String query, Pageable pageable) {
        return eventRepository.searchEvents(query, pageable);
    }

    @Override
    public Optional<Event> getPublishedEvent(UUID eventId) {
        return eventRepository.findEventByIdAndStatus(eventId, EventStatusEnum.PUBLISHED);
    }
}

