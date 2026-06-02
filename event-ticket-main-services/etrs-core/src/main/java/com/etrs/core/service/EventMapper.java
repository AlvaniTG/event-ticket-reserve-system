package com.etrs.core.service;

import com.etrs.core.domain.Event;
import com.etrs.core.dto.EventDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto.DetailsResponse toDetailsResponse(Event event);
    EventDto.SummaryResponse toSummaryResponse(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "REQUESTED")
    Event toEntity(EventDto.CreateRequest request);

    @Mapping(target = "startDate", source = "newStartDate")
    @Mapping(target = "endDate", source = "newEndDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityWithReschedule(@MappingTarget Event event, EventDto.RescheduleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    void updateEntity(@MappingTarget Event event, EventDto.UpdateRequest request);
}
