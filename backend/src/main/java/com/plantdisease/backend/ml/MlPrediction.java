package com.plantdisease.backend.ml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The JSON returned by FastAPI's /predict (snake_case names mapped to Java fields). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MlPrediction(
        @JsonProperty("top_class") MlClass topClass,
        boolean uncertain,
        double threshold,
        List<MlClass> top3,
        @JsonProperty("heatmap_base64") String heatmapBase64,
        @JsonProperty("inference_ms") double inferenceMs,
        @JsonProperty("gradcam_ms") Double gradcamMs) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MlClass(
            @JsonProperty("class_name") String className,
            double confidence) {
    }
}
