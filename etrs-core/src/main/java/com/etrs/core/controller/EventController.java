package com.etrs.core.controller;

import com.etrs.core.dto.EventDto;
import com.etrs.core.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventDto.SummaryResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventDto.DetailsResponse getEventById(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto.DetailsResponse createEvent(
            @Valid @RequestBody EventDto.CreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.createEvent(request, creatorId);
    }

    @PutMapping("/{id}")
    public EventDto.DetailsResponse updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventDto.UpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.updateEvent(id, request, creatorId);
    }

    @PatchMapping("/{id}/reschedule")
    public EventDto.DetailsResponse rescheduleEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventDto.RescheduleRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.rescheduleEvent(id, request, creatorId);
    }
}
