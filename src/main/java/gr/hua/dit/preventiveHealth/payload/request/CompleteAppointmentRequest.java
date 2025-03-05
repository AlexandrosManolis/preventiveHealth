package gr.hua.dit.preventiveHealth.payload.request;

import gr.hua.dit.preventiveHealth.entity.Appointment;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class CompleteAppointmentRequest {
    private String diagnosisDescription;

    public enum RecheckNeeded {
        YES, NO
    }

    public enum MedicalFileNeeded {
        YES, NO
    }

    @Enumerated(EnumType.STRING)
    private Appointment.RecheckNeeded recheckNeeded;

    @Enumerated(EnumType.STRING)
    private Appointment.MedicalFileNeeded medicalFileNeeded;

    private LocalDate recheckDate;

    private MultipartFile medicalFile;

    public MultipartFile getMedicalFile() {
        return medicalFile;
    }

    public void setMedicalFile(MultipartFile medicalFile) {
        this.medicalFile = medicalFile;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }

    public void setDiagnosisDescription(String diagnosisDescription) {
        this.diagnosisDescription = diagnosisDescription;
    }

    public Appointment.RecheckNeeded getRecheckNeeded() {
        return recheckNeeded;
    }

    public void setRecheckNeeded(Appointment.RecheckNeeded recheckNeeded) {
        this.recheckNeeded = recheckNeeded;
    }

    public Appointment.MedicalFileNeeded getMedicalFileNeeded() {
        return medicalFileNeeded;
    }

    public void setMedicalFileNeeded(Appointment.MedicalFileNeeded medicalFileNeeded) {
        this.medicalFileNeeded = medicalFileNeeded;
    }

    public LocalDate getRecheckDate() {
        return recheckDate;
    }

    public void setRecheckDate(LocalDate recheckDate) {
        this.recheckDate = recheckDate;
    }
}
