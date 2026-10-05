package com.plantdisease.backend.disease;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseService service;

    public DiseaseController(DiseaseService service) {
        this.service = service;
    }

    /** GET /api/diseases or /api/diseases?plant=Tomato */
    @GetMapping
    public List<DiseaseResponse> list(@RequestParam(required = false) String plant) {
        return service.findAll(plant);
    }

    /** GET /api/diseases/plants : the 14 plant names, for the filter in the interface */
    @GetMapping("/plants")
    public List<String> plants() {
        return service.findPlants();
    }

    /** GET /api/diseases/Tomato___Late_blight */
    @GetMapping("/{className}")
    public DiseaseResponse get(@PathVariable String className) {
        return service.findByClassName(className);
    }
}
