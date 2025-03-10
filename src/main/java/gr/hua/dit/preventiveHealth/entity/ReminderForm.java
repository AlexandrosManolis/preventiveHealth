package gr.hua.dit.preventiveHealth.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reminderForm")
public class ReminderForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer recurringTimeIntervalInDays;

    private String specialty;

    private String lastExam;

    private LocalDate lastExamDate;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;

    public ReminderForm() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getRecurringTimeIntervalInDays() {
        return recurringTimeIntervalInDays;
    }

    public void setRecurringTimeIntervalInDays(Integer recurringTimeIntervalInDays) {
        this.recurringTimeIntervalInDays = recurringTimeIntervalInDays;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public LocalDate getLastExamDate() {
        return lastExamDate;
    }

    public void setLastExamDate(LocalDate lastExamDate) {
        this.lastExamDate = lastExamDate;
    }

    public String getLastExam() {
        return lastExam;
    }

    public void setLastExam(String lastExam) {
        this.lastExam = lastExam;
    }
}
