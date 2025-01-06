package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.OpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Integer> {
    Optional<OpeningHours> findByDoctor_Id(Integer userId);

    Optional<OpeningHours> findByDiagnosticCenter_Id(Integer userId);
}
