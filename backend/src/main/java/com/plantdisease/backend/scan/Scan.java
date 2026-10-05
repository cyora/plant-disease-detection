package com.plantdisease.backend.scan;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.plantdisease.backend.disease.Disease;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** One analyzed photo: the most probable disease, the confidence, and where the images are stored. */
@Entity
@Table(name = "scan")
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "heatmap_path")
    private String heatmapPath;

    /** Most probable class, kept even when the result is uncertain. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Column(nullable = false)
    private double confidence;

    /** True when confidence is below the threshold: the interface shows "uncertain". */
    @Column(nullable = false)
    private boolean uncertain;

    @Column(nullable = false)
    private double threshold;

    @Column(name = "inference_ms")
    private Double inferenceMs;

    @Column(name = "gradcam_ms")
    private Double gradcamMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ScanPrediction> predictions = new ArrayList<>();

    protected Scan() {
        // required by JPA
    }

    public Scan(String imagePath, String heatmapPath, Disease disease, double confidence,
                boolean uncertain, double threshold, Double inferenceMs, Double gradcamMs) {
        this.imagePath = imagePath;
        this.heatmapPath = heatmapPath;
        this.disease = disease;
        this.confidence = confidence;
        this.uncertain = uncertain;
        this.threshold = threshold;
        this.inferenceMs = inferenceMs;
        this.gradcamMs = gradcamMs;
        this.createdAt = Instant.now();
    }

    public void addPrediction(Disease disease, int position, double confidence) {
        predictions.add(new ScanPrediction(this, disease, position, confidence));
    }

    public Long getId() { return id; }
    public String getImagePath() { return imagePath; }
    public String getHeatmapPath() { return heatmapPath; }
    public Disease getDisease() { return disease; }
    public double getConfidence() { return confidence; }
    public boolean isUncertain() { return uncertain; }
    public double getThreshold() { return threshold; }
    public Double getInferenceMs() { return inferenceMs; }
    public Double getGradcamMs() { return gradcamMs; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ScanPrediction> getPredictions() { return predictions; }
}
