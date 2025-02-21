package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.AppointmentDAO;
import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.*;
import gr.hua.dit.preventiveHealth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/appointment")
public class AppointmentRestController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentDAO appointmentDAO;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("{userId}/appointments")
    public ResponseEntity<?> getUserAppointment(@PathVariable Integer userId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        String userRole = userService.getUserRole();

        List<Appointment> appointments = new ArrayList<>();
        if(userRole.equals("ROLE_DOCTOR")){
            appointments = appointmentRepository.findByDoctorId(userId);
        } else if (userRole.equals("ROLE_PATIENT")) {
            appointments = appointmentRepository.findByPatientId(userId);
        } else if (userRole.equals("ROLE_DIAGNOSTIC")) {
            appointments = appointmentRepository.findByDiagnosticCenterId(userId);
        } else if (userRole.equals("ROLE_ADMIN")) {
            appointments = appointmentRepository.findAll();
        }
        appointments.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getTime));


        if(userId.equals(user.getId())){
            List<Appointment> storedAppointments = new ArrayList<>();
            for(Appointment appointment : appointments){

                if(userRole.equals("ROLE_DOCTOR") || userRole.equals("ROLE_DIAGNOSTIC")){
                    if ((appointment.getDoctor() != null && appointment.getDoctor().getUser().getId().equals(userId)) ||
                            (appointment.getDiagnosticCenter() != null && appointment.getDiagnosticCenter().getUser().getId().equals(userId))) {
                        if(appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED || (appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED && appointment.getAppointmentStatus() == Appointment.AppointmentStatus.CANCELLED)){
                            storedAppointments.add(appointment);
                        }
                    }
                } else if (userRole.equals("ROLE_PATIENT")) {
                    if(appointment.getPatient().getUser().getId().equals(userId)){
                        storedAppointments.add(appointment);
                    }
                }else {
                    return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
                }
            }
            return ResponseEntity.ok(storedAppointments);
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @GetMapping("{userId}/appointments/{appointmentId}/details")
    public ResponseEntity<?> getUserAppointmentDetails(@PathVariable Integer userId, @PathVariable Integer appointmentId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if(userId.equals(user.getId())){
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @PostMapping("{userId}/appointments/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer userId, @PathVariable Integer appointmentId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if(userId.equals(user.getId())){
            Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Not exist id: "+appointmentId));
            appointmentRepository.deleteById(appointmentId);
            return ResponseEntity.ok().body(new MessageResponse("Appointment cancelled"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @GetMapping("timeslots/{specialistId}")
    public ResponseEntity<?> generateTimeSlots(@PathVariable Integer specialistId, @RequestParam("date") String date) {
        LocalDate requestedDate = LocalDate.parse(date);

        List<String> slots = new ArrayList<>();
        LocalTime now = LocalTime.now().plusHours(3);
        Integer nowMinutes = now.getHour() * 60 + now.getMinute();

        List<Object[]> availableOpeningHours =appointmentDAO.availableOpeningHours(specialistId, requestedDate);

        if(availableOpeningHours.isEmpty()){
            return ResponseEntity.badRequest().body(new MessageResponse("Doctor is closed the date you requested."));
        }else{
            Integer startTime = appointmentDAO.convertToMinutes((String) availableOpeningHours.get(0)[0]);
            Integer endTime = appointmentDAO.convertToMinutes((String) availableOpeningHours.get(0)[1]) -20;
            boolean isToday = LocalDate.now().equals(date);

            List<Appointment> existAppointment = appointmentDAO.specialistPendingAppointment(specialistId, requestedDate);

            List<Integer> notAvailableTime = existAppointment.stream().map(Appointment::getTime)
                    .map(time -> appointmentDAO.convertToMinutes(time)).toList();

            for (int time = startTime; time < endTime; time += 30) {
                final int currentTime = time;
                boolean isNearUnavailableTime = notAvailableTime.stream()
                        .anyMatch(unavailableTime -> Math.abs(unavailableTime - currentTime) <= 10);

                if ((!isToday || time > nowMinutes) && (!notAvailableTime.contains(time)) && (!isNearUnavailableTime)) {
                    String hours = String.format("%02d", time / 60);
                    String minutes = String.format("%02d", time % 60);
                    slots.add(hours + ":" + minutes);
                }
            }
            return new ResponseEntity<>(slots, HttpStatus.OK);
        }
    }

    @PostMapping("request/{specialistId}")
    public ResponseEntity<?> saveAppointmentRequest(@PathVariable Integer specialistId,@RequestBody Appointment appointment){

        //set new appointment request
        Appointment appointmentRequest = new Appointment();

        //find the role of the specialist
        User specialist = userDAO.getUserProfile(specialistId);
        Boolean doctorRole = specialist.getRoles().stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getRoleName()));
        Boolean diagnosticRole = specialist.getRoles().stream().anyMatch(role -> "ROLE_DIAGNOSTIC".equals(role.getRoleName()));

        //get patient profile and role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer authUserId = userDAO.getUserId(username);
        Patient patient = patientRepository.findById(authUserId).orElseThrow(
                () -> new IllegalArgumentException("Patient not found")
        );
        String userRole = userService.getUserRole();


        List<Object[]> availableOpeningHours =appointmentDAO.availableOpeningHours(specialistId, appointment.getDate());
        if(availableOpeningHours.isEmpty()){
            return ResponseEntity.badRequest().body(new MessageResponse("Doctor is closed the date you requested."));
        }

        //check if appointment time is in past
        LocalTime appointmentTime = LocalTime.parse(appointment.getTime(), DateTimeFormatter.ofPattern("HH:mm"));

        if (appointment.getDate().isBefore(LocalDate.now()) ||
                (appointment.getDate().isEqual(LocalDate.now()) && appointmentTime.isBefore(LocalTime.now()))) {
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to request a past appointment"));
        }

        if(appointmentDAO.existUnrejectedAppointment(specialistId, appointment.getDate(),appointment.getTime())){
            return ResponseEntity.ok().body(new MessageResponse("Doctor has already an appointment at this time."));
        }

        //set the appointment
        if (userRole.equals("ROLE_PATIENT")) {
            appointmentRequest.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.PENDING);
            appointmentRequest.setAppointmentStatus(Appointment.AppointmentStatus.PENDING);
            appointmentRequest.setPatient(patient);
            appointmentRequest.setTime(appointment.getTime());
            appointmentRequest.setDate(appointment.getDate());

            if(doctorRole){
                Doctor doctor = doctorRepository.findById(specialistId).orElseThrow(
                        () -> new IllegalArgumentException("Doctor not found")
                );
                if(doctor.getUser().getRegisterRequest().getStatus().equals(RegisterRequest.Status.PENDING) || doctor.getUser().getRegisterRequest().getStatus().equals(RegisterRequest.Status.REJECTED)){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Doctor will join the platform soon"));
                }
                appointmentRequest.setSpecialty(appointment.getSpecialty());
                appointmentRequest.setDoctor(doctor);
            }else if(diagnosticRole) {
                DiagnosticCenter diagnosticCenter = diagnosticRepository.findById(specialistId).orElseThrow(
                        () -> new IllegalArgumentException("DiagnosticCenter not found")
                );
                if(diagnosticCenter.getUser().getRegisterRequest().getStatus().equals(RegisterRequest.Status.PENDING) || diagnosticCenter.getUser().getRegisterRequest().getStatus().equals(RegisterRequest.Status.REJECTED)){
                    return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to request an appointment"));
                }
                if (diagnosticCenter.getSpecialties().contains(appointment.getSpecialty())) {
                    appointmentRequest.setSpecialty(appointment.getSpecialty());
                }else {
                    return ResponseEntity.badRequest().body(new MessageResponse("No specialty found"));
                }
                appointmentRequest.setDiagnosticCenter(diagnosticCenter);

            }
            appointmentRepository.save(appointmentRequest);
            return ResponseEntity.ok().body(new MessageResponse("Appointment request saved successfully"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to request an appointment"));
        }
    }
}