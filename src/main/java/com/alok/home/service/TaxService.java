package com.alok.home.service;

import com.alok.home.commons.entity.Tax;
import com.alok.home.commons.repository.TaxRepository;
import com.alok.home.commons.dto.api.response.TaxesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaxService {

    private final TaxRepository taxRepository;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public TaxService(TaxRepository taxRepository) {
        this.taxRepository = taxRepository;
    }

    public TaxesResponse getAllTaxes() {
        log.info("All Taxes not available in cache");

        List<Tax> taxes = taxRepository.findAll();

        return TaxesResponse.builder()
                .taxes(taxes.stream()
                        .map(tax -> TaxesResponse.Tax.builder()
                                .id(tax.getId())
                                .financialYear(tax.getFinancialYear())
                                .paidAmount(tax.getPaidAmount())
                                .refundAmount(tax.getRefundAmount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void saveAllTaxes(List<Tax> taxRecords) {
        log.info("Delete all the taxes first");
        taxRepository.deleteAll();

        log.info("Save all the taxes");
        taxRepository.saveAll(taxRecords);
    }
}
