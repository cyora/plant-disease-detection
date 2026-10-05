package com.plantdisease.backend.scan;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;

import com.plantdisease.backend.disease.Disease;
import com.plantdisease.backend.disease.DiseaseResponse;

/** JSON formats returned by the scan endpoints. */
public final class ScanResponses {

    private ScanResponses() {
    }

    /** One line of the history. */
    public record ScanSummary(
            Long id,
            Instant createdAt,
            String className,
            String plant,
            String diseaseName,
            boolean healthy,
            double confidence,
            boolean uncertain,
            String imageUrl) {

        static ScanSummary from(Scan s) {
            Disease d = s.getDisease();
            return new ScanSummary(s.getId(), s.getCreatedAt(), d.getClassName(), d.getPlant(),
                    d.getDiseaseName(), d.isHealthy(), s.getConfidence(), s.isUncertain(), ScanResponses.imageUrl(s));
        }
    }

    /** One of the top-3 predictions. */
    public record PredictionItem(
            int position,
            String className,
            String plant,
            String diseaseName,
            boolean healthy,
            double confidence) {

        static PredictionItem from(ScanPrediction p) {
            Disease d = p.getDisease();
            return new PredictionItem(p.getPosition(), d.getClassName(), d.getPlant(),
                    d.getDiseaseName(), d.isHealthy(), p.getConfidence());
        }
    }

    /** The full result of a scan, shown on the result page. */
    public record ScanDetail(
            Long id,
            Instant createdAt,
            boolean uncertain,
            double confidence,
            double threshold,
            DiseaseResponse disease,
            List<PredictionItem> top3,
            String imageUrl,
            String heatmapUrl,
            Double inferenceMs,
            Double gradcamMs) {

        static ScanDetail from(Scan s) {
            return new ScanDetail(s.getId(), s.getCreatedAt(), s.isUncertain(), s.getConfidence(),
                    s.getThreshold(), DiseaseResponse.from(s.getDisease()),
                    s.getPredictions().stream().map(PredictionItem::from).toList(),
                    ScanResponses.imageUrl(s),
                    s.getHeatmapPath() != null ? "/api/scans/" + s.getId() + "/heatmap" : null,
                    s.getInferenceMs(), s.getGradcamMs());
        }
    }

    /** A page of results, in a stable format for the interface. */
    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {

        static <T> PageResponse<T> from(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages());
        }
    }

    private static String imageUrl(Scan s) {
        return "/api/scans/" + s.getId() + "/image";
    }
}
