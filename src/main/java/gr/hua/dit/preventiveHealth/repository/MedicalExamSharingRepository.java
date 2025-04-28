package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExamSharing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicalExamSharingRepository extends JpaRepository<MedicalExamSharing, Integer> {

    List<MedicalExamSharing> findAllByDoctorId(Integer doctorId);

    List<MedicalExamSharing> findAllByDiagnosticCenterId(Integer userId);

    void deleteByExpirationTimeLessThanEqual(LocalDateTime now);
}
