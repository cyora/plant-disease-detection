package com.plantdisease.backend.stats;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plantdisease.backend.scan.ScanRepository;
import com.plantdisease.backend.stats.StatsResponse.DailyCount;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final int DAYS = 30;

    private final ScanRepository repository;

    public StatsService(ScanRepository repository) {
        this.repository = repository;
    }

    public StatsResponse stats() {
        Double avg = repository.averageConfidence();
        return new StatsResponse(
                repository.count(),
                repository.countByUncertain(false),
                repository.countByUncertain(true),
                repository.countConfidentByHealthy(true),
                repository.countConfidentByHealthy(false),
                avg == null ? 0 : Math.round(avg * 10000) / 10000.0,
                repository.topDiseases(PageRequest.of(0, 5)),
                repository.countByPlant(),
                scansPerDay());
    }

    /** Number of scans per day over the last 30 days, including days with zero scans. */
    private List<DailyCount> scansPerDay() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate first = today.minusDays(DAYS - 1);
        Instant since = first.atStartOfDay(zone).toInstant();

        Map<LocalDate, Long> counts = repository.findCreatedAtSince(since).stream()
                .collect(Collectors.groupingBy(i -> LocalDate.ofInstant(i, zone), Collectors.counting()));

        List<DailyCount> result = new ArrayList<>();
        for (LocalDate day = first; !day.isAfter(today); day = day.plusDays(1)) {
            result.add(new DailyCount(day, counts.getOrDefault(day, 0L)));
        }
        return result;
    }
}
