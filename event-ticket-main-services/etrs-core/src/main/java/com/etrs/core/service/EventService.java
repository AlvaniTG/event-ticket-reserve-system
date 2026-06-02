package com.etrs.core.service;

import com.etrs.core.domain.Event;
import com.etrs.core.dto.EventDto;
import com.etrs.core.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public EventDto.DetailsResponse getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(eventMapper::toDetailsResponse)
                .orElseThrow(() -> new EntityNotFoundException("Event with id: " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<EventDto.SummaryResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toSummaryResponse)
                .toList();
    }

    public EventDto.DetailsResponse createEvent(EventDto.CreateRequest request, UUID creatorId) {
        Event event = eventMapper.toEntity(request);
        event.setCreatorId(creatorId);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDetailsResponse(savedEvent);
    }

    public EventDto.DetailsResponse updateEvent(UUID id, EventDto.UpdateRequest request, UUID creatorId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id: " + id + " not found"));

        event.verifyOwnership(creatorId);

        eventMapper.updateEntity(event, request);
        return eventMapper.toDetailsResponse(eventRepository.save(event));
    }

    public EventDto.DetailsResponse rescheduleEvent(UUID id, EventDto.RescheduleRequest request, UUID creatorId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event with id: " + id + " not found"));

        event.verifyOwnership(creatorId);
        event.changeStatusAfterReschedule();
        eventMapper.updateEntityWithReschedule(event, request);

        return eventMapper.toDetailsResponse(eventRepository.save(event));
    }
}
