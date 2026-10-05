package com.plantdisease.backend.disease;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One of the 38 classes the model can predict, with the information shown to the user.
 * Rows are inserted by data.sql; the application only reads them.
 */
@Entity
@Table(name = "disease")
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Exactly as in class_names.json, e.g. "Tomato___Late_blight". Links the model output to this row. */
    @Column(name = "class_name", nullable = false, unique = true, length = 100)
    private String className;

    @Column(nullable = false, length = 50)
    private String plant;

    @Column(name = "disease_name", nullable = false, length = 100)
    private String diseaseName;

    @Column(nullable = false)
    private boolean healthy;

    @Column(length = 150)
    private String pathogen;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prevention;

    protected Disease() {
        // required by JPA
    }

    public Long getId() { return id; }
    public String getClassName() { return className; }
    public String getPlant() { return plant; }
    public String getDiseaseName() { return diseaseName; }
    public boolean isHealthy() { return healthy; }
    public String getPathogen() { return pathogen; }
    public String getDescription() { return description; }
    public String getSymptoms() { return symptoms; }
    public String getTreatment() { return treatment; }
    public String getPrevention() { return prevention; }
}
