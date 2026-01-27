package org.elkased.eventticketplatform.controllers;


import lombok.RequiredArgsConstructor;
import org.elkased.eventticketplatform.domain.dtos.GetPublishedEventDetailsResponseDto;
import org.elkased.eventticketplatform.domain.dtos.ListPublisedEventResponseDto;
import org.elkased.eventticketplatform.mappers.EventMapper;
import org.elkased.eventticketplatform.services.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/published-events")
@RequiredArgsConstructor
public class PublishedEventController {
    private final EventMapper eventMapper;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<ListPublisedEventResponseDto>> listPublishedEvents(
            @RequestParam(name = "q", required = false) String q,
            Pageable pageable
    ) {
        if (null != q && !q.trim().isEmpty()) {
            return ResponseEntity.ok(
                    eventService.searchPublishedEvents(q, pageable)
                            .map(eventMapper::toListPublisedEventResponseDto)
            );
        }
        return ResponseEntity.ok(
                eventService.listPublishedEvents(pageable)
                        .map(eventMapper::toListPublisedEventResponseDto)
        );
    }

    @GetMapping("{eventId}")
    public ResponseEntity<GetPublishedEventDetailsResponseDto> getPublishedEvent(
            @PathVariable UUID eventId
    ) {
        return eventService.getPublishedEvent(eventId)
                .map(eventMapper::toGetPublishedEventDetailsResponseDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
