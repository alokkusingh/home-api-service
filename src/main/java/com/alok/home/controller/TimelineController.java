package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.EventTimelineResponseDTO;
import com.alok.home.commons.dto.api.response.LifeEventResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/timeline")
@Tag(name = "Timeline", description = "Life Events related to timeline")
public abstract class TimelineController {

    @Operation(
            summary = "Get all life events",
            description = "Returns all life events available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LifeEventResponseDTO.class))
            ),
            @ApiResponse(responseCode = "204", description = "No life events found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/family")
    public abstract ResponseEntity<List<LifeEventResponseDTO>> getLifeEvents();

    @Operation(
            summary = "Get all events",
            description = "Returns all events available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EventTimelineResponseDTO.class))
            ),
            @ApiResponse(responseCode = "204", description = "No events found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/events")
    public abstract ResponseEntity<List<EventTimelineResponseDTO>> getEvents();

    @Operation(
            summary = "Get event details by id",
            description = "Returns the event details for the specified ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LifeEventResponseDTO.EventDTO.class))
            ),
            @ApiResponse(responseCode = "204", description = "No events found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/events/{id}")
    public abstract ResponseEntity<LifeEventResponseDTO.EventDTO> getEventDetails(
            @PathVariable(value = "id") String id
    );
}
