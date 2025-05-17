package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.entity.ReminderForm;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.ReminderFormRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.PatientRepository;
import gr.hua.dit.preventiveHealth.service.ReminderFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("api/reminder")
public class ReminderFormRestController {

    @Autowired
    private ReminderFormRepository reminderFormRepository;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ReminderFormService reminderFormService;

    @GetMapping("form/{userId}/getForms")
    public ResponseEntity<?> patientReminderForms(@PathVariable Integer userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer patientId = userDAO.getUserId(username);
        Map<String, List> appointmentsAndReminders = new HashMap<>();

        List<ReminderForm> allReminderForms;
        List<Appointment> allAppointments;

        if(patientId.equals(userId)){
             allReminderForms = reminderFormRepository.findByPatientId(userId);

             allAppointments = appointmentRepository.findByPatientId(userId);
             allAppointments.removeIf(appointment -> appointment.getAppointmentStatus() != Appointment.AppointmentStatus.PENDING && appointment.getAppointmentRequestStatus() != Appointment.AppointmentRequestStatus.APPROVED);
             appointmentsAndReminders.put("appointments", allAppointments);

             appointmentsAndReminders.put("reminderForms", allReminderForms);

             return new ResponseEntity<>(appointmentsAndReminders, HttpStatus.OK);
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @PostMapping("form/{userId}/save")
    public ResponseEntity<?> savePatientForm(@PathVariable Integer userId, @RequestBody Map<String, ReminderForm> reminderForms) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer patientId = userDAO.getUserId(auth.getName());

        // Check authorization
        if (!patientId.equals(userId)) {
            return new ResponseEntity<>("Unauthorized access", HttpStatus.FORBIDDEN);
        }

        try {
            Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));

            // Get existing forms
            List<ReminderForm> existingForms = reminderFormRepository.findByPatientId(patientId);
            List<ReminderForm> formEntities = new ArrayList<>();

            // Incoming forms
            for (ReminderForm form : reminderForms.values()) {
                // Find matching existing form by specialty
                ReminderForm existingForm = existingForms.stream().filter(f -> f.getSpecialty().equals(form.getSpecialty())).findFirst().orElse(null);

                if (existingForm != null) {
                    // Update existing form
                    existingForm.setLastExam(form.getLastExam());
                    existingForm.setLastExamDate(form.getLastExamDate());
                    existingForm.setRecurringTimeIntervalInDays(form.getRecurringTimeIntervalInDays());
                    formEntities.add(existingForm);
                } else {
                    // Create new form
                    form.setPatient(patient);
                    formEntities.add(form);
                }
            }

            // Find forms to delete
            List<ReminderForm> formsToDelete = existingForms.stream().filter(existingForm -> formEntities.stream()
                    .noneMatch(f -> f.getSpecialty().equals(existingForm.getSpecialty()))).collect(Collectors.toList());

            // Delete and save forms
            reminderFormRepository.deleteAll(formsToDelete);
            reminderFormRepository.saveAll(formEntities);

            // Calculate next exam dates
            reminderFormService.updateNextExamDate(patientId);

            return new ResponseEntity<>(formEntities, HttpStatus.OK);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + "Error saving form.");
        }
    }
}
