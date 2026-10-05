package com.plantdisease.backend.disease;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    Optional<Disease> findByClassName(String className);

    /** All classes sorted by plant, diseases first, healthy last. */
    List<Disease> findAllByOrderByPlantAscHealthyAscDiseaseNameAsc();

    List<Disease> findByPlantIgnoreCaseOrderByHealthyAscDiseaseNameAsc(String plant);

    @Query("select distinct d.plant from Disease d order by d.plant")
    List<String> findDistinctPlants();
}
