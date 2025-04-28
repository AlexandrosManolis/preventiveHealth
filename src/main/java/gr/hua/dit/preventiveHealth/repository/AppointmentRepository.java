package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByPatientId(Integer userId);

    List<Appointment> findByDoctorId(Integer userId);

    List<Appointment> findByDiagnosticCenterId(Integer userId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE a.patient.id = :patientId AND a.doctor.id = :userId")
    Boolean existsByPatientIdAndDoctorId(Integer patientId, Integer userId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE a.patient.id = :patientId AND a.diagnosticCenter.id = :userId")
    Boolean existsByPatientIdAndDiagnosticId(Integer patientId, Integer userId);
}
