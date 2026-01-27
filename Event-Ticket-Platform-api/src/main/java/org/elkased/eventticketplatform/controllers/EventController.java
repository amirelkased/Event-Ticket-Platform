package org.elkased.eventticketplatform.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.CreateEventRequest;
import org.elkased.eventticketplatform.domain.dtos.*;
import org.elkased.eventticketplatform.domain.entities.Event;
import org.elkased.eventticketplatform.mappers.EventMapper;
import org.elkased.eventticketplatform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventMapper eventMapper;
    private final EventService eventService;

    @PostMapping(value = "create")
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CreateEventRequestDto request
    ) {
        CreateEventRequest createEventRequest = eventMapper.fromDto(request);
        UUID userId = parseUserId(jwt);
        Event createdEvent = eventService.createEvent(userId, createEventRequest);
        CreateEventResponseDto dto = eventMapper.toCreateEventResponseDto(createdEvent);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ListEventResponseDto>> getAllEvents(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        UUID organizerId = parseUserId(jwt);
        Page<Event> events = eventService.listEventsForOrganizer(organizerId, pageable);
        Page<ListEventResponseDto> listEventResponseDto = events.map(eventMapper::toListEventResponseDto);
        return ResponseEntity.ok(listEventResponseDto);
    }

    @GetMapping("{eventId}")
    public ResponseEntity<GetEventDetailsResponseDto> getEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ) {
        UUID user = parseUserId(jwt);
        Optional<Event> eventForOrganizer = eventService.getEventForOrganizer(eventId, user);
        return eventForOrganizer.map(eventMapper::toGetEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("{eventId}")
    public ResponseEntity<UpdateEventResponseDto> updateEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId,
            @RequestBody @Valid UpdateEventRequestDto updateEventRequestDto
    ) {
        UUID user = parseUserId(jwt);
        Event event = eventService.updateEventForOrganizer(user, eventId, eventMapper.fromDto(updateEventRequestDto));
        return new ResponseEntity<>(eventMapper.toUpdateEventResponseDto(event), HttpStatus.OK);
    }

    @DeleteMapping("{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID eventId
    ) {
        UUID user = parseUserId(jwt);
        eventService.deleteEvent(user, eventId);
        return ResponseEntity.noContent().build();
    }

    private UUID parseUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
