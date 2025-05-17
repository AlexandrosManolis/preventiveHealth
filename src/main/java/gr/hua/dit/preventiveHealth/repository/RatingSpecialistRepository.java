package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.RatingSpecialist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingSpecialistRepository extends JpaRepository<RatingSpecialist, Integer> {
    Boolean existsByDoctorIdAndPatientId(Integer userId, Integer patientId);

    Boolean existsByDiagnosticCenterIdAndPatientId(Integer diagnosticCenterId, Integer patientId);

    @Query("SELECT r.rating FROM RatingSpecialist r WHERE r.doctor.id = :userId AND r.patient.id = :patientId")
    Integer findRatingByDoctorIdAndPatientId(@Param("userId") Integer userId, @Param("patientId") Integer patientId);

    @Query("SELECT r.rating FROM RatingSpecialist r WHERE r.diagnosticCenter.id = :diagnosticCenterId AND r.patient.id = :patientId")
    Integer findRatingByDiagnosticCenterIdAndPatientId(@Param("diagnosticCenterId") Integer diagnosticCenterId, @Param("patientId") Integer patientId);

    List<RatingSpecialist> getRatingSpecialistByDoctorId(Integer userId);

    List<RatingSpecialist> getRatingSpecialistByDiagnosticCenterId(Integer userId);
}
