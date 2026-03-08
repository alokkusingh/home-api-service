package com.alok.home.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/cache")
@Tag(name = "Cache", description = "APIs to manage application caches")
public abstract class CacheController {

    @Operation(
            summary = "Evict all caches",
            description = "Clears all application caches. Returns an optional resource or status indicating the result."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caches evicted successfully",
                    content = @Content(mediaType = "application/octet-stream",
                            schema = @Schema(implementation = Resource.class))
            ),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/evict-all")
    public abstract ResponseEntity<Resource> evictAllCache();
}