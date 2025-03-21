package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.MedicalExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalExamRepository extends JpaRepository<MedicalExam, Integer> {
}
