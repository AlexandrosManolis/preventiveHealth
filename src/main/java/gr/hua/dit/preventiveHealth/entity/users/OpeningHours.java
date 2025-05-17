package gr.hua.dit.preventiveHealth.entity.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.time.DayOfWeek;

@Entity
@Table(name = "openingHours", uniqueConstraints = @UniqueConstraint (columnNames = {"doctor_id", "diagnostic_center_id"}))
public class OpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonBackReference("doctor-schedule")
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private Doctor doctor;

    @JsonBackReference("diagnostic-schedule")
    @ManyToOne
    @JoinColumn(name = "diagnostic_center_id", nullable = true)
    private DiagnosticCenter diagnosticCenter;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Invalid time format")
    private String startTime;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Invalid time format")
    private String endTime;

    public OpeningHours() {
    }

    public OpeningHours(DayOfWeek dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public OpeningHours(DiagnosticCenter diagnosticCenter, DayOfWeek dayOfWeek, String startTime, String endTime) {
        this.diagnosticCenter = diagnosticCenter;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
