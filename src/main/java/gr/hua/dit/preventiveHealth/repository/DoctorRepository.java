package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}
