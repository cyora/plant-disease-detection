package com.plantdisease.backend.disease;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class DiseaseService {

    private final DiseaseRepository repository;

    public DiseaseService(DiseaseRepository repository) {
        this.repository = repository;
    }

    public List<DiseaseResponse> findAll(String plant) {
        List<Disease> diseases = (plant == null || plant.isBlank())
                ? repository.findAllByOrderByPlantAscHealthyAscDiseaseNameAsc()
                : repository.findByPlantIgnoreCaseOrderByHealthyAscDiseaseNameAsc(plant);
        return diseases.stream().map(DiseaseResponse::from).toList();
    }

    public DiseaseResponse findByClassName(String className) {
        return repository.findByClassName(className)
                .map(DiseaseResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unknown disease class: " + className));
    }

    public List<String> findPlants() {
        return repository.findDistinctPlants();
    }
}
