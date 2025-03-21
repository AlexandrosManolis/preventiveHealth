package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.entity.ReminderForm;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.ReminderFormRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.SpecialtiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReminderFormService {

    @Autowired
    private SpecialtiesRepository specialtiesRepository;

    @Autowired
    private ReminderFormRepository reminderFormRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;


    public void updateNextExamDate(Integer userId) {
        getNextExamDate(reminderFormRepository.findByPatientId(userId), appointmentRepository.findByPatientId(userId), userId);
    }

    private void getNextExamDate(List<ReminderForm> reminderForms, List<Appointment> allAppointments, Integer userId) {
        for (ReminderForm form : reminderForms) {

            // Calculate default date based on recurring interval or specialty recommendation
            LocalDate defaultDate;
            if (form.getRecurringTimeIntervalInDays() == null) {
                Integer months = specialtiesRepository.findRecheckIntervalBySpecialty(form.getSpecialty());
                months = (months != null) ? months : 12;

                defaultDate = form.getLastExamDate().plusMonths(months);
            } else {
                defaultDate = form.getLastExamDate().plusDays(form.getRecurringTimeIntervalInDays());
            }

            // Find latest appointment for this specialty
            Optional<Appointment> latestAppointment = allAppointments.stream()
                    .filter(a -> a.getSpecialty().equals(form.getSpecialty()))
                    .max(Comparator.comparing(Appointment::getDate));

            LocalDate nextDate = defaultDate;

            if (latestAppointment.isPresent()) {
                Appointment appointment = latestAppointment.get();

                // Pending approved appointment
                if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.PENDING &&
                        appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED) {
                    nextDate = appointment.getDate();
                }
                // Completed appointment with recheck date
                else if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED &&
                        appointment.getRecheckDate() != null) {
                    nextDate = appointment.getRecheckDate();
                } else if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED && appointment.getRecheckDate() == null) {
                    if(appointment.getDate().isAfter(form.getLastExamDate())) {
                        if(form.getRecurringTimeIntervalInDays() != null) {
                            nextDate = appointment.getDate().plusDays(form.getRecurringTimeIntervalInDays());
                        }else {
                            Integer months = specialtiesRepository.findRecheckIntervalBySpecialty(form.getSpecialty());
                            months = (months != null) ? months : 12;
                            nextDate = appointment.getDate().plusMonths(months);
                        }
                    }
                }
            }

            form.setNextExamDateReminder(nextDate);
            reminderFormRepository.save(form);
        }
    }
}
