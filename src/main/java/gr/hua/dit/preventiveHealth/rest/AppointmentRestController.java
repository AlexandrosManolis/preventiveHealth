package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.AppointmentDAO;
import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.payload.request.CompleteAppointmentRequest;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.*;
import gr.hua.dit.preventiveHealth.repository.usersRepository.DiagnosticRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.DoctorRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.PatientRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import gr.hua.dit.preventiveHealth.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private MedicalExamService medicalExamService;

    @Autowired
    private ReminderFormService reminderFormService;

    @Autowired
    private GmailService gmailService;

    @GetMapping("{userId}/allAppointments")
    public ResponseEntity<?> getAllAppointments(@PathVariable Integer userId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        String userRole = userService.getUserRole();

        List<Appointment> appointments = new ArrayList<>();
        if(userRole.equals("ROLE_DOCTOR")){
            appointments = appointmentRepository.findByDoctorId(userId);
        } else if (userRole.equals("ROLE_DIAGNOSTIC")) {
            appointments = appointmentRepository.findByDiagnosticCenterId(userId);
        } else if (userRole.equals("ROLE_ADMIN")) {
            appointments = appointmentRepository.findAll();
        }
        appointments.sort(Comparator.comparing(Appointment::getDate).reversed().thenComparing(Appointment::getTime));


        if(userId.equals(user.getId())){
            List<Map<String, Object>> storedAppointments = new ArrayList<>();

            for (Appointment appointment : appointments) {
                if (userRole.equals("ROLE_DOCTOR") || userRole.equals("ROLE_DIAGNOSTIC")) {
                    if ((appointment.getDoctor() != null && appointment.getDoctor().getUser().getId().equals(userId)) ||
                            (appointment.getDiagnosticCenter() != null && appointment.getDiagnosticCenter().getUser().getId().equals(userId))) {

                        Map<String, Object> appointmentData = new HashMap<>();
                        appointmentData.put("date", appointment.getDate());
                        appointmentData.put("appointmentStatus", appointment.getAppointmentStatus());

                        storedAppointments.add(appointmentData);
                    }
                }
            }
            return ResponseEntity.ok(storedAppointments);
        }else {
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @GetMapping("{userId}/medicalRecord")
    public ResponseEntity<?> getCompletedAppointments(@PathVariable Integer userId){
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
                        if(appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED ){
                            storedAppointments.add(appointment);
                        }
                    }
                } else if (userRole.equals("ROLE_PATIENT")) {
                    if(appointment.getPatient().getUser().getId().equals(userId) && appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED){
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

    @GetMapping("{userId}/uncompletedAppointments")
    public ResponseEntity<?> getUncompletedAppointments(@PathVariable Integer userId){
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
                        if(appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED && appointment.getAppointmentStatus() != Appointment.AppointmentStatus.COMPLETED){
                            storedAppointments.add(appointment);
                        }
                    }
                } else if (userRole.equals("ROLE_PATIENT")) {
                    if(appointment.getPatient().getUser().getId().equals(userId) && appointment.getAppointmentStatus() != Appointment.AppointmentStatus.COMPLETED){
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

    @GetMapping("{userId}/pendingAppointments")
    public ResponseEntity<?> getPendingAppointment(@PathVariable Integer userId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer authenticatedId = userDAO.getUserId(username);
        String userRole = userService.getUserRole();

        List<Appointment> appointments= new ArrayList<>();
        try {
            if(authenticatedId.equals(userId) && userRole.equals("ROLE_DOCTOR")){
                List<Appointment> allAppointments = appointmentRepository.findByDoctorId(userId);
                for(Appointment appointment : allAppointments){
                    if(appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.PENDING){
                        appointments.add(appointment);
                    }
                }
            }else if (authenticatedId.equals(userId) && userRole.equals("ROLE_DIAGNOSTIC")){
                List<Appointment> allAppointments = appointmentRepository.findByDiagnosticCenterId(userId);
                for(Appointment appointment : allAppointments){
                    if(appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.PENDING){
                        appointments.add(appointment);
                    }
                }
            }
            return ResponseEntity.ok(appointments);
        }catch (Exception err){
            return ResponseEntity.badRequest().body(err);
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
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer userId, @PathVariable Integer appointmentId, @RequestBody String causeOfRejection){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userRole = userService.getUserRole();
        Integer authenticatedId = userDAO.getUserId(username);

        User user = userRepository.findByUsername(username).orElseThrow();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Not exist id: "+appointmentId));
        boolean isAuthorizedDoctor = false;
        boolean isAuthorizedDiagnostic = false;
        if(userRole.equals("ROLE_DOCTOR") && authenticatedId.equals(userId)){
            isAuthorizedDoctor = appointment.getDoctor().getUser().getId().equals(userId);
        } else if (userRole.equals("ROLE_DIAGNOSTIC") && authenticatedId.equals(userId)) {
            isAuthorizedDiagnostic = appointment.getDiagnosticCenter().getUser().getId().equals(userId);
        }

        if(userId.equals(user.getId()) || isAuthorizedDoctor || isAuthorizedDiagnostic){
            if(appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.PENDING){
                appointment.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.REJECTED);
            }
            appointment.setAppointmentStatus(Appointment.AppointmentStatus.CANCELLED);
            if(causeOfRejection == null ){
                return ResponseEntity.badRequest().body(new MessageResponse("Cause of Rejection is required"));
            }
            appointment.setRejectionCause(causeOfRejection);
            appointmentRepository.save(appointment);

            if(appointment.getDoctor() != null){
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Cancelled","Your appointment with "+ appointment.getDoctor().getFullName() + " has been cancelled");
                gmailService.sendEmail(appointment.getDoctor().getUser().getEmail(),"Appointment Cancelled","Your appointment with "+ appointment.getPatient().getFullName() + " has been cancelled");
            } else if (appointment.getDiagnosticCenter() != null) {
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Cancelled","Your appointment with "+ appointment.getDiagnosticCenter().getFullName() + " has been cancelled");
                gmailService.sendEmail(appointment.getDiagnosticCenter().getUser().getEmail(),"Appointment Cancelled","Your appointment with "+ appointment.getPatient().getFullName() + " has been cancelled");
            }

            // Calculate next exam dates
            reminderFormService.updateNextExamDate(appointment.getPatient().getId());

            return ResponseEntity.ok().body(new MessageResponse("Appointment cancelled"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @PostMapping("{userId}/appointments/{appointmentId}/accept")
    public ResponseEntity<?> acceptAppointment(@PathVariable Integer userId, @PathVariable Integer appointmentId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userRole = userService.getUserRole();
        Integer authenticatedId = userDAO.getUserId(username);

        User user = userRepository.findByUsername(username).orElseThrow();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Not exist id: "+appointmentId));
        boolean isAuthorizedDoctor = false;
        boolean isAuthorizedDiagnostic = false;

        if(userRole.equals("ROLE_DOCTOR") && authenticatedId.equals(userId)){
            isAuthorizedDoctor = appointment.getDoctor().getUser().getId().equals(userId);
        } else if (userRole.equals("ROLE_DIAGNOSTIC") && authenticatedId.equals(userId)) {
            isAuthorizedDiagnostic = appointment.getDiagnosticCenter().getUser().getId().equals(userId);
        }

        LocalDate today = LocalDate.now();

        if((userId.equals(user.getId()) || isAuthorizedDoctor || isAuthorizedDiagnostic) &&
                (!appointment.getDate().isBefore(today) && appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.PENDING)){

            appointment.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.APPROVED);

            appointmentRepository.save(appointment);

            if(appointment.getDoctor() != null){
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Accepted","Your appointment with "+ appointment.getDoctor().getFullName() + " has been accepted");
                gmailService.sendEmail(appointment.getDoctor().getUser().getEmail(),"Appointment Accepted","Your appointment with "+ appointment.getPatient().getFullName() + " has been accepted");
            } else if (appointment.getDiagnosticCenter() != null) {
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Accepted","Your appointment with "+ appointment.getDiagnosticCenter().getFullName() + " has been accepted");
                gmailService.sendEmail(appointment.getDiagnosticCenter().getUser().getEmail(),"Appointment Accepted","Your appointment with "+ appointment.getPatient().getFullName() + " has been accepted");
            }

            // Calculate next exam dates
            reminderFormService.updateNextExamDate(appointment.getPatient().getId());

            return ResponseEntity.ok().body(new MessageResponse("Appointment accepted"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @PostMapping("{userId}/appointments/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Integer userId, @PathVariable Integer appointmentId, @ModelAttribute CompleteAppointmentRequest completeAppointmentRequest){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userRole = userService.getUserRole();
        Integer authenticatedId = userDAO.getUserId(username);

        User user = userRepository.findByUsername(username).orElseThrow();
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Not exist id: "+appointmentId));
        boolean isAuthorizedDoctor = false;
        boolean isAuthorizedDiagnostic = false;

        if(userRole.equals("ROLE_DOCTOR") && authenticatedId.equals(userId)){
            isAuthorizedDoctor = appointment.getDoctor().getUser().getId().equals(userId);
        } else if (userRole.equals("ROLE_DIAGNOSTIC") && authenticatedId.equals(userId)) {
            isAuthorizedDiagnostic = appointment.getDiagnosticCenter().getUser().getId().equals(userId);
        }

        if(userId.equals(user.getId()) || isAuthorizedDoctor || isAuthorizedDiagnostic){
            if (completeAppointmentRequest.getDiagnosisDescription() == null || completeAppointmentRequest.getDiagnosisDescription().isEmpty() ||
                    completeAppointmentRequest.getMedicalFileNeeded() == null || completeAppointmentRequest.getRecheckNeeded() == null) {

                return ResponseEntity.badRequest().body(new MessageResponse("Diagnosis description, medical file needed, and recheck needed are required"));
            }

            if (completeAppointmentRequest.getMedicalFileNeeded().equals(Appointment.MedicalFileNeeded.YES) &&
                    (completeAppointmentRequest.getMedicalFile() == null || completeAppointmentRequest.getMedicalFile().isEmpty())) {

                return ResponseEntity.badRequest().body(new MessageResponse("Medical file is required when medical file is marked as needed"));
            }

            if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.PENDING) {
                appointment.setAppointmentStatus(Appointment.AppointmentStatus.COMPLETED);
                appointment.setDiagnosisDescription(completeAppointmentRequest.getDiagnosisDescription());
                appointment.setRecheckNeeded(completeAppointmentRequest.getRecheckNeeded());
                appointment.setMedicalFileNeeded(completeAppointmentRequest.getMedicalFileNeeded());
                appointment.setRecheckDate(completeAppointmentRequest.getRecheckDate());

                if (completeAppointmentRequest.getMedicalFile() != null &&
                        !completeAppointmentRequest.getMedicalFile().isEmpty()) {

                    medicalExamService.uploadExam(appointmentId, completeAppointmentRequest.getMedicalFile());
                }
            } else if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED) {
                appointment.setDiagnosisDescription(completeAppointmentRequest.getDiagnosisDescription());

                if (completeAppointmentRequest.getMedicalFile() != null &&
                        !completeAppointmentRequest.getMedicalFile().isEmpty()) {

                    medicalExamService.uploadExam(appointmentId, completeAppointmentRequest.getMedicalFile());
                }
            }

            appointmentRepository.save(appointment);

            if(appointment.getDoctor() != null){
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Completed","Your appointment with "+ appointment.getDoctor().getFullName() + " has been completed");
                gmailService.sendEmail(appointment.getDoctor().getUser().getEmail(),"Appointment Completed","Your appointment with "+ appointment.getPatient().getFullName() + " has been completed");
            } else if (appointment.getDiagnosticCenter() != null) {
                gmailService.sendEmail(appointment.getPatient().getUser().getEmail(),"Appointment Completed","Your appointment with "+ appointment.getDiagnosticCenter().getFullName() + " has been completed");
                gmailService.sendEmail(appointment.getDiagnosticCenter().getUser().getEmail(),"Appointment Completed","Your appointment with "+ appointment.getPatient().getFullName() + " has been completed");
            }

            // Calculate next exam dates
            reminderFormService.updateNextExamDate(appointment.getPatient().getId());

            return ResponseEntity.ok().body(new MessageResponse("Appointment accepted"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to access resource"));
        }
    }

    @GetMapping("timeslots/{specialistId}")
    public ResponseEntity<?> generateTimeSlots(@PathVariable Integer specialistId, @RequestParam("date") String date, @RequestParam("specialty") String specialty) {
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
            boolean isToday = LocalDate.now().equals(requestedDate);

            Doctor doctor = doctorRepository.findById(specialistId).orElse(null);
            DiagnosticCenter diagnostic = diagnosticRepository.findById(specialistId).orElse(null);
            boolean isSpecialtyValid = false;

            if (doctor != null) {
                isSpecialtyValid = doctor.getSpecialty().equalsIgnoreCase(specialty);
            } else if (diagnostic != null) {
                isSpecialtyValid = diagnostic.getSpecialties().stream().anyMatch(s -> s.equalsIgnoreCase(specialty));
            }

            List<Appointment> existingAppointment = new ArrayList<>();
            if(isSpecialtyValid){
                existingAppointment = appointmentDAO.specialistPendingAppointment(specialistId, requestedDate, specialty);
            }

            List<Integer> notAvailableTime = existingAppointment.stream().map(Appointment::getTime)
                    .map(time -> appointmentDAO.convertToMinutes(time)).toList();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            List<Integer> patientBookedTimes = new ArrayList<>();

            if(authentication != null && authentication.isAuthenticated()){
                String username = authentication.getName();
                Integer authUserId = userDAO.getUserId(username);

                List<Appointment> patientAppointments = appointmentDAO.patientPendingAppointments(authUserId, requestedDate);

                patientBookedTimes = patientAppointments.stream()
                        .map(appointment -> appointmentDAO.convertToMinutes(appointment.getTime())).toList();
            }

            for (int time = startTime; time < endTime; time += 30) {
                final int currentTime = time;
                boolean isNearUnavailableTime = notAvailableTime.stream()
                        .anyMatch(unavailableTime -> Math.abs(unavailableTime - currentTime) <= 10);

                if ((!isToday || time > nowMinutes) && (!notAvailableTime.contains(time)) && (!isNearUnavailableTime) && (!patientBookedTimes.contains(time))) {
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

        if(appointmentDAO.existUnrejectedAppointment(specialistId, appointment.getDate(),appointment.getTime(),appointment.getSpecialty())){
            return ResponseEntity.ok().body(new MessageResponse("Doctor has already an appointment at this time."));
        }

        //set the appointment
        if (userRole.equals("ROLE_PATIENT")) {
            appointmentRequest.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.PENDING);
            appointmentRequest.setAppointmentStatus(Appointment.AppointmentStatus.PENDING);
            appointmentRequest.setPatient(patient);
            appointmentRequest.setTime(appointment.getTime());
            appointmentRequest.setDate(appointment.getDate());
            appointmentRequest.setAppointmentCause(appointment.getAppointmentCause());

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

            if(appointmentRequest.getDoctor() != null){
                gmailService.sendEmail(appointmentRequest.getPatient().getUser().getEmail(),"New Appointment Request","Your appointment request with "+ appointmentRequest.getDoctor().getFullName() + " has been submitted");
                gmailService.sendEmail(appointmentRequest.getDoctor().getUser().getEmail(),"New Appointment Request","Your appointment request with "+ appointmentRequest.getPatient().getFullName() + " has been submitted");
            } else if (appointmentRequest.getDiagnosticCenter() != null) {
                gmailService.sendEmail(appointmentRequest.getPatient().getUser().getEmail(),"New Appointment Request","Your appointment request with "+ appointmentRequest.getDiagnosticCenter().getFullName() + " has been submitted");
                gmailService.sendEmail(appointmentRequest.getDiagnosticCenter().getUser().getEmail(),"New Appointment Request","Your appointment request with "+ appointmentRequest.getPatient().getFullName() + " has been submitted");
            }
            return ResponseEntity.ok().body(new MessageResponse("Appointment request saved successfully"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to request an appointment"));
        }
    }

    @PostMapping("request/{specialistId}/change")
    public ResponseEntity<?> changeAppointmentRequest(@PathVariable Integer specialistId,@RequestBody Appointment appointment){

        Appointment existedAppointment = appointmentRepository.findById(appointment.getId()).orElseThrow(() -> new ResourceNotFoundException("Not exist id: "+appointment.getId()));
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

        if(appointmentDAO.existUnrejectedAppointment(specialistId, appointment.getDate(),appointment.getTime(),appointment.getSpecialty())){
            return ResponseEntity.ok().body(new MessageResponse("Doctor has already an appointment at this time."));
        }

        //set the appointment
        if (userRole.equals("ROLE_PATIENT")) {
            existedAppointment.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.PENDING);
            existedAppointment.setAppointmentStatus(Appointment.AppointmentStatus.PENDING);
            existedAppointment.setTime(appointment.getTime());
            existedAppointment.setDate(appointment.getDate());
            existedAppointment.setAppointmentCause(appointment.getAppointmentCause());
            existedAppointment.setRejectionCause(null);

            appointmentRepository.save(existedAppointment);

            if(existedAppointment.getDoctor() != null){
                gmailService.sendEmail(existedAppointment.getPatient().getUser().getEmail(),"New Appointment Request Change","Your appointment request with "+ existedAppointment.getDoctor().getFullName() + " has been changed");
                gmailService.sendEmail(existedAppointment.getDoctor().getUser().getEmail(),"New Appointment Request Change","Your appointment request with "+ existedAppointment.getPatient().getFullName() + " has been changed");
            } else if (existedAppointment.getDiagnosticCenter() != null) {
                gmailService.sendEmail(existedAppointment.getPatient().getUser().getEmail(),"New Appointment Request Change","Your appointment request with "+ existedAppointment.getDiagnosticCenter().getFullName() + " has been changed");
                gmailService.sendEmail(existedAppointment.getDiagnosticCenter().getUser().getEmail(),"New Appointment Request Change","Your appointment request with "+ existedAppointment.getPatient().getFullName() + " has been changed");
            }

            // Calculate next exam dates
            reminderFormService.updateNextExamDate(existedAppointment.getPatient().getId());

            return ResponseEntity.ok().body(new MessageResponse("Appointment request saved successfully"));
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("You are not allowed to request an appointment"));
        }
    }
}