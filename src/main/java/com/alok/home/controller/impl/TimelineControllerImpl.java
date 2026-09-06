package com.alok.home.controller.impl;

import com.alok.home.commons.dto.api.response.EventTimelineResponseDTO;
import com.alok.home.commons.dto.api.response.LifeEventResponseDTO;
import com.alok.home.commons.utils.annotation.runtime.LogExecutionTime;
import com.alok.home.controller.TimelineController;
import com.alok.home.service.TimelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TimelineControllerImpl extends TimelineController {

    private final TimelineService timelineService;
    private final Long cacheControlMaxAge;

    public TimelineControllerImpl(TimelineService timelineService,
                                  @Value("${web.cache-control.max-age}") Long cacheControlMaxAge) {
        this.timelineService = timelineService;
        this.cacheControlMaxAge = cacheControlMaxAge;
    }

    @LogExecutionTime
    @Override
    public ResponseEntity<List<LifeEventResponseDTO>> getLifeEvents() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(timelineService.getGlobalProfilesTimeline());
    }

    @LogExecutionTime
    @Override
    public ResponseEntity<List<EventTimelineResponseDTO>> getEvents() {
        log.info("Fetching flat event timeline dataset for logged in user session");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(timelineService.getGlobalEventsTimeline());
    }

    @LogExecutionTime
    @Override
    public ResponseEntity<LifeEventResponseDTO.EventDTO> getEventDetails(@PathVariable(value = "id") String id) {
        log.info("Fetching flat event timeline for logged in user session");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(cacheControlMaxAge, TimeUnit.SECONDS).noTransform().mustRevalidate())
                .body(timelineService.getLifeEventDetails(Long.parseLong(id)));
    }
}
