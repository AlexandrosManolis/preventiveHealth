package gr.hua.dit.preventiveHealth.entity.medicalExams;

import jakarta.persistence.*;

@Entity
@Table(name = "downloadToken")
public class DownloadToken {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    private String verificationToken;

    @OneToOne
    @JoinColumn(name = "medicalExam_id", nullable = false)
    private MedicalExam medicalExam;

    public enum Status{
        PENDING, COMPLETED
    }

    private Status status;

    public DownloadToken() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public MedicalExam getMedicalExam() {
        return medicalExam;
    }

    public void setMedicalExam(MedicalExam medicalExam) {
        this.medicalExam = medicalExam;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
