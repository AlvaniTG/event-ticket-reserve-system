package com.etrs.core.controller;

import com.etrs.core.dto.EventDto;
import com.etrs.core.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Events", description = "Endpoints for managing event catalog")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(
            summary = "Get all events",
            description = "Retrieves basic information about all events."
    )
    public List<EventDto.SummaryResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get event by ID",
            description = "Retrieves detailed information about event with given ID."
    )
    public EventDto.DetailsResponse getEventById(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create an event",
            description = "Allows authorized user to create an event."
    )
    public EventDto.DetailsResponse createEvent(
            @Valid @RequestBody EventDto.CreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.createEvent(request, creatorId);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Change event details",
            description = "Allows authorized user to change event details."
    )
    public EventDto.DetailsResponse updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventDto.UpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.updateEvent(id, request, creatorId);
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(
            summary = "Reschedule event",
            description = "Allows authorized user to change events starting and/or ending date."
    )
    public EventDto.DetailsResponse rescheduleEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventDto.RescheduleRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID creatorId = UUID.fromString(jwt.getSubject());
        return eventService.rescheduleEvent(id, request, creatorId);
    }
}
