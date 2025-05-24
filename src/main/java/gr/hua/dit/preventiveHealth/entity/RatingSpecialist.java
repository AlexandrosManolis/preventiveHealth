package gr.hua.dit.preventiveHealth.entity;

import gr.hua.dit.preventiveHealth.entity.users.DiagnosticCenter;
import gr.hua.dit.preventiveHealth.entity.users.Doctor;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "rating_specialist",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"patientId", "doctorId"}),
                @UniqueConstraint(columnNames = {"patientId", "diagnosticId"})
        })
public class RatingSpecialist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private Double rating;

    private String ratingDescription;

    @ManyToOne
    @JoinColumn(name = "doctorId")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "diagnosticId")
    private DiagnosticCenter diagnosticCenter;

    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    private Patient patient;

    public RatingSpecialist() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getRatingDescription() {
        return ratingDescription;
    }

    public void setRatingDescription(String ratingDescription) {
        this.ratingDescription = ratingDescription;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}