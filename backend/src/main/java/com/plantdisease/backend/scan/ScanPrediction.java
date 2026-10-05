package com.plantdisease.backend.scan;

import com.plantdisease.backend.disease.Disease;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One of the 3 best predictions of a scan (position 1, 2 or 3). */
@Entity
@Table(name = "scan_prediction")
public class ScanPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id", nullable = false)
    private Scan scan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    /** 1 = most probable. Not named "rank", which is a reserved word in MySQL 8. */
    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private double confidence;

    protected ScanPrediction() {
        // required by JPA
    }

    ScanPrediction(Scan scan, Disease disease, int position, double confidence) {
        this.scan = scan;
        this.disease = disease;
        this.position = position;
        this.confidence = confidence;
    }

    public Long getId() { return id; }
    public Scan getScan() { return scan; }
    public Disease getDisease() { return disease; }
    public int getPosition() { return position; }
    public double getConfidence() { return confidence; }
}
