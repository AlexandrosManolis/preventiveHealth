package gr.hua.dit.preventiveHealth.entity.medicalExams;

import gr.hua.dit.preventiveHealth.entity.users.DiagnosticCenter;
import gr.hua.dit.preventiveHealth.entity.users.Doctor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fileSharing")
public class MedicalExamSharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "medicalExam_id", nullable = false)
    private MedicalExam medicalExam;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "diagnostic_id")
    private DiagnosticCenter diagnosticCenter;

    private LocalDateTime expirationTime;

    public MedicalExamSharing() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MedicalExam getMedicalExam() {
        return medicalExam;
    }

    public void setMedicalExam(MedicalExam medicalExam) {
        this.medicalExam = medicalExam;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public DiagnosticCenter getDiagnosticCenter() {
        return diagnosticCenter;
    }

    public void setDiagnosticCenter(DiagnosticCenter diagnosticCenter) {
        this.diagnosticCenter = diagnosticCenter;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
