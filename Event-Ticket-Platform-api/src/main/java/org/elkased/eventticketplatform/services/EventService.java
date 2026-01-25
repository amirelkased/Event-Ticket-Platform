package org.elkased.eventticketplatform.services;

import org.elkased.eventticketplatform.domain.CreateEventRequest;
import org.elkased.eventticketplatform.domain.UpdateEventRequest;
import org.elkased.eventticketplatform.domain.entities.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface EventService {
    Event createEvent(UUID organizerId, CreateEventRequest event);

    Page<Event> listEventsForOrganizer(UUID organizerId, Pageable pageable);

    Optional<Event> getEventForOrganizer(UUID eventId, UUID organizerId);

    Event updateEventForOrganizer(UUID organizerId,UUID eventId, UpdateEventRequest updateEventRequest);
}
