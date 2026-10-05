package com.plantdisease.backend.stats;

import java.time.LocalDate;
import java.util.List;

/** Everything the dashboard needs, in one call. Healthy/diseased counts only use confident scans. */
public record StatsResponse(
        long totalScans,
        long confidentScans,
        long uncertainScans,
        long healthyScans,
        long diseasedScans,
        double averageConfidence,
        List<CountItem> topDiseases,
        List<CountItem> scansByPlant,
        List<DailyCount> scansLast30Days) {

    public record DailyCount(LocalDate date, long count) {
    }
}
