package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentDAO {

    Boolean existUnrejectedAppointment(Integer specialistId, LocalDate date, String time, String specialty);

    List<Object[]> availableOpeningHours(Integer specialistId, LocalDate date);

    List<Appointment> specialistPendingAppointment(Integer specialistId, LocalDate date, String specialty);

    Integer convertToMinutes(String time);

    List<Appointment> patientPendingAppointments(Integer authUserId, LocalDate requestedDate);
}
