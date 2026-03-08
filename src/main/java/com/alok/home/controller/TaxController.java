package com.alok.home.controller;

import com.alok.home.commons.dto.api.response.TaxesResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tax")
public abstract class TaxController {

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public abstract ResponseEntity<TaxesResponse> getAllTaxes();
}
