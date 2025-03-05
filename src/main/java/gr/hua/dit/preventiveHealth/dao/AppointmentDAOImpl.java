package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class AppointmentDAOImpl implements AppointmentDAO {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Boolean existUnrejectedAppointment(Integer specialistId, LocalDate date, String time, String specialty){
        try {
            Long exists = entityManager.createQuery(
                    "SELECT COUNT(a) FROM Appointment a " +
                       "WHERE (a.doctor.id =: specialistId OR a.diagnosticCenter.id =: specialistId) AND a.date =: date AND a.time =: time AND (a.appointmentRequestStatus = 'ACCEPTED' OR a.appointmentRequestStatus = 'PENDING') AND (a.appointmentStatus = 'PENDING') AND (a.specialty = :specialty)", Long.class)
                    .setParameter("specialistId", specialistId)
                    .setParameter("date", date)
                    .setParameter("time", time)
                    .setParameter("specialty", specialty)
                    .getSingleResult();
            return exists >0;
        }catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public List<Object[]> availableOpeningHours(Integer specialistId, LocalDate date) {
        try {
            return entityManager.createQuery(
                            "SELECT s.startTime, s.endTime FROM OpeningHours s " +
                                    "WHERE (s.diagnosticCenter.id = :specialistId OR s.doctor.id = :specialistId) " +
                                    "AND s.dayOfWeek = :dayOfWeek",
                            Object[].class)
                    .setParameter("specialistId", specialistId)
                    .setParameter("dayOfWeek", date.getDayOfWeek())
                    .getResultList();

        } catch (NoResultException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Appointment> specialistPendingAppointment(Integer specialistId, LocalDate date, String specialty) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM Appointment a " +
                                    "WHERE (a.diagnosticCenter.id = :specialistId OR a.doctor.id = :specialistId) " +
                                    "AND a.date = :date  AND (a.appointmentRequestStatus = 'APPROVED' OR a.appointmentRequestStatus = 'PENDING') AND a.appointmentStatus = 'PENDING' AND a.specialty = :specialty"
                            ,Appointment.class)
                    .setParameter("specialistId", specialistId)
                    .setParameter("date", date)
                    .setParameter("specialty", specialty)
                    .getResultList();

        } catch (NoResultException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Appointment> patientPendingAppointments(Integer patientId, LocalDate date) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM Appointment a " +
                                    "WHERE a.patient.id = :patientId " +
                                    "AND a.date = :date " +
                                    "AND (a.appointmentRequestStatus = 'APPROVED' OR a.appointmentRequestStatus = 'PENDING') " +
                                    "AND a.appointmentStatus = 'PENDING'", Appointment.class)
                    .setParameter("patientId", patientId)
                    .setParameter("date", date)
                    .getResultList();
        } catch (NoResultException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    public Integer convertToMinutes(String time) {

        if (time == null || !time.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Invalid time format. Expected format is HH:mm."+ time);
        }

        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        //total minutes
        return hours * 60 + minutes;
    }

}
