package gr.hua.dit.preventiveHealth.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.ReminderForm;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import gr.hua.dit.preventiveHealth.repository.ReminderFormRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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

    @GetMapping("form/{userId}/exists")
    public ResponseEntity<?> patientHasFormFilled(@PathVariable Integer userId) {
        Boolean exists = reminderFormRepository.existsByPatientId(userId);

        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @PostMapping("form/{userId}/save")
    public ResponseEntity<?> savePatientForm(@PathVariable Integer userId, @RequestBody Map<String, ReminderForm> reminderForms) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer patientId = userDAO.getUserId(username);

        List<ReminderForm> formEntities = new ArrayList<>();
        try {
            if (patientId.equals(userId) && !reminderFormRepository.existsByPatientId(userId)) {
                Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new RuntimeException("Patient not found"));

                for (ReminderForm form : reminderForms.values()) {
                    form.setPatient(patient);
                    formEntities.add(form);
                }

                reminderFormRepository.saveAll(formEntities);
            }
            return new ResponseEntity<>(formEntities, HttpStatus.OK);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + "Error saving form.");
        }
    }


}
