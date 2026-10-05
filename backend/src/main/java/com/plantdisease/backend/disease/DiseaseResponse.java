package com.plantdisease.backend.disease;

/** What the API returns for a disease (keeps the entity separate from the JSON format). */
public record DiseaseResponse(
        Long id,
        String className,
        String plant,
        String diseaseName,
        boolean healthy,
        String pathogen,
        String description,
        String symptoms,
        String treatment,
        String prevention) {

    public static DiseaseResponse from(Disease d) {
        return new DiseaseResponse(d.getId(), d.getClassName(), d.getPlant(), d.getDiseaseName(),
                d.isHealthy(), d.getPathogen(), d.getDescription(), d.getSymptoms(),
                d.getTreatment(), d.getPrevention());
    }
}
