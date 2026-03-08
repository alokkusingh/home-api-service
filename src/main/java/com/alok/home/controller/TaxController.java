// java
package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.TaxesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tax")
@Tag(name = "Tax", description = "APIs to query tax related data")
public abstract class TaxController {

    @Operation(
            summary = "Get all taxes",
            description = "Returns all tax records available in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful response",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaxesResponse.class))
            ),
            @ApiResponse(responseCode = "204", description = "No taxes found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TaxesResponse> getAllTaxes();
}