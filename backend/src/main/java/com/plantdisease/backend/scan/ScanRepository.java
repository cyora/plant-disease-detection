package com.plantdisease.backend.scan;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.plantdisease.backend.stats.CountItem;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    /** History page: loads each scan with its disease in one query (avoids the N+1 problem). */
    @EntityGraph(attributePaths = "disease")
    Page<Scan> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** One scan with its disease and its 3 predictions. */
    @EntityGraph(attributePaths = {"disease", "predictions", "predictions.disease"})
    Optional<Scan> findWithDetailsById(Long id);

    // ---------- Statistics ----------

    long countByUncertain(boolean uncertain);

    @Query("select count(s) from Scan s where s.uncertain = false and s.disease.healthy = :healthy")
    long countConfidentByHealthy(@Param("healthy") boolean healthy);

    @Query("select avg(s.confidence) from Scan s")
    Double averageConfidence();

    @Query("""
            select new com.plantdisease.backend.stats.CountItem(concat(d.plant, ' - ', d.diseaseName), count(s))
            from Scan s join s.disease d
            where s.uncertain = false and d.healthy = false
            group by d.plant, d.diseaseName
            order by count(s) desc""")
    List<CountItem> topDiseases(Pageable pageable);

    @Query("""
            select new com.plantdisease.backend.stats.CountItem(d.plant, count(s))
            from Scan s join s.disease d
            where s.uncertain = false
            group by d.plant
            order by count(s) desc""")
    List<CountItem> countByPlant();

    @Query("select s.createdAt from Scan s where s.createdAt >= :since")
    List<Instant> findCreatedAtSince(@Param("since") Instant since);
}
