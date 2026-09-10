package com.alok.home.controller;

import com.alok.home.commons.entity.MedicalLabResult;
import com.alok.home.commons.entity.MedicalMemberLab;
import com.alok.home.commons.repository.MedicalLabResultRepository;
import com.alok.home.commons.repository.MedicalMemberLabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/medical")
@CrossOrigin(origins = "*") // Allows your Web UI frontend port to access data without CORS blocks
public class MedicalReportController {

    @Autowired
    private MedicalMemberLabRepository medicalMemberLabRepository;

    @Autowired
    private MedicalLabResultRepository medicalLabResultRepository;

    /**
     * Get the full historical timeline of all lab visits for a specific family member.
     * GET /api/medical/timeline/1
     */
    @GetMapping("/timeline/{entityId}")
    public ResponseEntity<List<Map<String, Object>>> getMemberTimeline(@PathVariable Long entityId) {
        List<MedicalMemberLab> labs = medicalMemberLabRepository.findByEntityIdOrderByTestDateDesc(entityId);

        List<Map<String, Object>> result = labs.stream().map(lab -> {
            Map<String, Object> labMap = new LinkedHashMap<>();
            labMap.put("labRecordId", lab.getLabRecordId());
            labMap.put("testDate", lab.getTestDate());

            List<Map<String, Object>> metricsList = lab.getResults().stream().map(res -> {
                Map<String, Object> resMap = new LinkedHashMap<>();
                resMap.put("metricName", res.getMetric().getMetricName());
                resMap.put("value", res.getRecordedValue());
                resMap.put("unit", res.getMetric().getUnit());
                resMap.put("minNormal", res.getMetric().getMinNormalValue());
                resMap.put("maxNormal", res.getMetric().getMaxNormalValue());
                resMap.put("isOutOfRange", res.getIsOutOfRange());
                return resMap;
            }).collect(Collectors.toList());

            labMap.put("results", metricsList);
            return labMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Get historical trend points for a specific metric to plot line graphs.
     * GET /api/medical/trends?entityId=1&metricId=5
     */
    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getMetricTrends(
            @RequestParam Long entityId,
            @RequestParam Integer metricId) {

        List<MedicalLabResult> trendResults = medicalLabResultRepository.findHistoricalTrends(entityId, metricId);

        List<Map<String, Object>> chartData = trendResults.stream().map(res -> {
            Map<String, Object> dataPoint = new LinkedHashMap<>();
            dataPoint.put("date", res.getMemberLab().getTestDate().toString());
            dataPoint.put("value", res.getRecordedValue());
            dataPoint.put("isOutOfRange", res.getIsOutOfRange());
            return dataPoint;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(chartData);
    }
}
