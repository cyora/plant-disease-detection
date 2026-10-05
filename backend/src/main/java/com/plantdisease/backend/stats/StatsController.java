package com.plantdisease.backend.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    /** GET /api/stats : numbers for the dashboard. */
    @GetMapping
    public StatsResponse stats() {
        return service.stats();
    }
}
