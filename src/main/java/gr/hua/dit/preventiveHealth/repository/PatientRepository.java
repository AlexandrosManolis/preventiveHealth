package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
}
