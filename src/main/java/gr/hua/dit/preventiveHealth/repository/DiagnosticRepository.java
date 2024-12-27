package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.DiagnosticCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DiagnosticRepository extends JpaRepository<DiagnosticCenter, Integer> {

    List<DiagnosticCenter> findBySpecialties(String specialty);
}
