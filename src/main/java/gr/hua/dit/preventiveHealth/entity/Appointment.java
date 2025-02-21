package gr.hua.dit.preventiveHealth.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "appointment")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private LocalDate date;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Invalid time format")
    private String time;

    @NotBlank
    private String specialty;

    public enum AppointmentStatus {
        PENDING, COMPLETED, CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @NotNull
    private AppointmentStatus appointmentStatus;

    public enum AppointmentRequestStatus {
        PENDING,APPROVED,REJECTED
    }

    @Enumerated(EnumType.STRING)
    @NotNull
    private AppointmentRequestStatus appointmentRequestStatus;

    private String appointmentCause;

    private String examDescription;

    private String rejectionCause;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patientId", referencedColumnName = "id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctorId", referencedColumnName = "id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "diagnosticId", referencedColumnName = "id")
    private DiagnosticCenter diagnosticCenter;

    public Appointment() {
    }

    public Appointment(LocalDate date, String time, String specialty) {
        this.date = date;
        this.time = time;
        this.specialty = specialty;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public AppointmentRequestStatus getAppointmentRequestStatus() {
        return appointmentRequestStatus;
    }

    public void setAppointmentRequestStatus(AppointmentRequestStatus appointmentRequestStatus) {
        this.appointmentRequestStatus = appointmentRequestStatus;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public String getRejectionCause() {
        return rejectionCause;
    }

    public String getAppointmentCause() {
        return appointmentCause;
    }

    public void setAppointmentCause(String appointmentCause) {
        this.appointmentCause = appointmentCause;
    }

    public void setRejectionCause(String rejectionCause) {
        this.rejectionCause = rejectionCause;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", date=" + date +
                ", time='" + time + '\'' +
                ", appointmentStatus=" + appointmentStatus +
                ", appointmentRequestStatus=" + appointmentRequestStatus +
                '}';
    }

}
