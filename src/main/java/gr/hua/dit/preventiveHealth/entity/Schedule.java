package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import javax.tools.Diagnostic;
import java.time.DayOfWeek;

@Entity
@Table(name = "schedules", uniqueConstraints = @UniqueConstraint (columnNames = {"doctor_id", "diagnostic_center_id"}))
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "diagnostic_center_id", nullable = true)
    private DiagnosticCenter diagnosticCenter;

    @NotBlank
    private String specialty;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Pattern(regexp = "^([0-1]\\\\d|2[0-3]):[0-5]\\\\d$", message = "Invalid time format")
    private String startTime;

    @Pattern(regexp = "^([0-1]\\\\d|2[0-3]):[0-5]\\\\d$", message = "Invalid time format")
    private String endTime;

    public Schedule() {
    }

    public Schedule(Doctor doctor, String specialty, DayOfWeek dayOfWeek, String startTime, String endTime) {
        this.doctor = doctor;
        this.specialty = specialty;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Schedule(DiagnosticCenter diagnosticCenter, String specialty, DayOfWeek dayOfWeek, String startTime, String endTime) {
        this.diagnosticCenter = diagnosticCenter;
        this.specialty = specialty;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
