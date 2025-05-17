package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalExamRepository extends JpaRepository<MedicalExam, Integer> {
    List<MedicalExam> findByPatientId(Integer patientId);
}
