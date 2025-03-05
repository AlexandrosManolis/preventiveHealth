package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.*;
import gr.hua.dit.preventiveHealth.repository.usersRepository.*;
import gr.hua.dit.preventiveHealth.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.preventiveHealth.payload.validation.Update;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/user")
public class UserRestController{

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterRequestRepository registerRequestRepository;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OpeningHoursRepository openingHoursRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiagnosticRepository diagnosticRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private SpecialtiesRepository specialtiesRepository;

    @GetMapping("find_specialist")
    public ResponseEntity<?> getAllSpecialties(@RequestParam (required = false) String specialty) {
        List<User> allSpecialists = new ArrayList<>();
        List<Doctor> doctors;
        List<DiagnosticCenter> diagnostics;


        if(specialty == null) {
            List<String> allSpecialties = userDAO.getAllSpecialties();
            return new ResponseEntity<>(allSpecialties, HttpStatus.OK);
        }else if (specialty.equals("all")) {
            doctors = doctorRepository.findAll();
            diagnostics = diagnosticRepository.findAll();
        } else {
            diagnostics = diagnosticRepository.findBySpecialties(specialty);
            doctors = doctorRepository.findBySpecialty(specialty);
        }

        for (Doctor doctor : doctors) {
            if(doctor.getUser().getRegisterRequest().getStatus() == RegisterRequest.Status.ACCEPTED) {
                User user = new User();
                Doctor doctorDetails = new Doctor();

                user.setId(doctor.getUser().getId());
                user.setRoles(doctor.getUser().getRoles());
                user.setFullName(doctor.getUser().getFullName());
                doctorDetails.setAddress(doctor.getAddress());
                doctorDetails.setCity(doctor.getCity());
                doctorDetails.setState(doctor.getState());
                doctorDetails.setSpecialty(doctor.getSpecialty());

                user.setDoctor(doctorDetails);
                allSpecialists.add(user);
            }
        }

        for (DiagnosticCenter diagnostic : diagnostics) {
            if (diagnostic.getUser().getRegisterRequest().getStatus() == RegisterRequest.Status.ACCEPTED) {
                User user = new User();
                DiagnosticCenter diagnosticCenter = new DiagnosticCenter();

                user.setId(diagnostic.getUser().getId());
                user.setRoles(diagnostic.getUser().getRoles());
                user.setFullName(diagnostic.getUser().getFullName());
                diagnosticCenter.setAddress(diagnostic.getAddress());
                diagnosticCenter.setCity(diagnostic.getCity());
                diagnosticCenter.setState(diagnostic.getState());
                diagnosticCenter.setSpecialties(diagnostic.getSpecialties());

                user.setDiagnosticCenter(diagnosticCenter);
                allSpecialists.add(user);
            }
        }
        return new ResponseEntity<>(allSpecialists, HttpStatus.OK);
    }

    @GetMapping("specialist/{userId}/details")
    public ResponseEntity<?> specialistDetails(@PathVariable Integer userId) {
        User user = userDAO.getUserProfile(userId);
        user.setPassword(null);

        if (user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()))) {
            return ResponseEntity.badRequest().body(Map.of("error", "This id is not for a specialist"));
        } else if (user.getRoles().stream().anyMatch(role -> "ROLE_PATIENT".equals(role.getRoleName()))) {
            return ResponseEntity.badRequest().body(Map.of("error", "This id is not for a specialist"));
        } else {
            return ResponseEntity.ok(user);
        }
    }

    @GetMapping("specialties")
    public ResponseEntity<?> getAllSpecialties() {
        List<Specialties> specialties = specialtiesRepository.findAll();
        return new ResponseEntity<>(specialties, HttpStatus.OK);
    }

    @GetMapping("{userId}/profile")
    public ResponseEntity<?> userProfile(@PathVariable Integer userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String userRole = userService.getUserRole();
        Integer authUserId = userDAO.getUserId(username);

        User user = userDAO.getUserProfile(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND); // Return 404 if user does not exist
        }

        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isOwner = userId.equals(authUserId);

        System.out.println(isOwner);
        if (!isOwner && !isAdmin) {
            return new ResponseEntity<>("Unauthorized to access this profile", HttpStatus.UNAUTHORIZED);
        }
        user.setPassword(null);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Transactional
    @Validated(Update.class)
    @PostMapping("{userId}/edit-profile")
    public ResponseEntity<?> editUser(@PathVariable Integer userId, @RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer authUserId = userDAO.getUserId(username);
        String userRole = userService.getUserRole();
        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        System.out.println("Received User: " + user);
        // Fetch the user to be edited
        User the_user = (User) userService.getUser(userId);

        if (the_user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Check authorization
        if (!authUserId.equals(userId) && !isAdmin) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.FORBIDDEN);
        }

        if (!the_user.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (!the_user.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        if (the_user.getRoles().stream().anyMatch(role -> "ROLE_DIAGNOSTIC".equals(role.getRoleName())) && !the_user.getDiagnosticCenter().getAfm().equals(user.getDiagnosticCenter().getAfm())
                && userRepository.existsByDiagnosticCenter_Afm(user.getDiagnosticCenter().getAfm())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Afm is already in use!"));
        }

        if (the_user.getRoles().stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getRoleName())) && !the_user.getDoctor().getAfm().equals(user.getDoctor().getAfm())
                && userRepository.existsByDoctor_Afm(user.getDoctor().getAfm())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Afm is already in use!"));
        }

        if (the_user.getRoles().stream().anyMatch(role -> "ROLE_PATIENT".equals(role.getRoleName())) && !the_user.getPatient().getAmka().equals(user.getPatient().getAmka())
                && userRepository.existsByPatient_Amka(user.getPatient().getAmka())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Amka is already in use!"));
        }

        // Update common fields
        the_user.setUsername(user.getUsername());
        the_user.setEmail(user.getEmail());
        the_user.setFullName(user.getFullName());
        the_user.setPhoneNumber(user.getPhoneNumber());

        // Handle role-specific updates
        if (the_user.getRoles().stream().anyMatch(role -> "ROLE_PATIENT".equals(role.getRoleName()))) {
            handlePatientUpdate(the_user, user);
        } else if (the_user.getRoles().stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getRoleName()))) {
            handleDoctorUpdate(the_user, user);
        } else if (the_user.getRoles().stream().anyMatch(role -> "ROLE_DIAGNOSTIC".equals(role.getRoleName()))) {
            handleDiagnosticCenterUpdate(the_user, user);
        }

        // Save changes
        try {
            userDAO.saveUser(the_user);

            // Update authentication principal if necessary
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User userDetails = (User) authentication.getPrincipal();
                if (userDetails.getUsername().equals(the_user.getUsername())) {
                    userDetails.setUsername(the_user.getUsername());
                    userDetails.setEmail(the_user.getEmail());
                }
            }

            return new ResponseEntity<>(the_user, HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = "Error saving user: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse(errorMessage));
        }
    }

    private void handlePatientUpdate(User the_user, User user) {
        Patient patient = the_user.getPatient();
        if (patient == null) patient = new Patient();

        patient.setAmka(user.getPatient().getAmka());
        patient.setGender(user.getPatient().getGender());
        patient.setBirthday(user.getPatient().getBirthday());
        patient.setUser(the_user);

        patientRepository.save(patient);
        the_user.setPatient(patient);
    }

    private void handleDoctorUpdate(User the_user, User user) {
        Doctor doctor = the_user.getDoctor();
        if (doctor == null) doctor = new Doctor();

        doctor.setAddress(user.getDoctor().getAddress());
        doctor.setCity(user.getDoctor().getCity());
        doctor.setState(user.getDoctor().getState());
        doctor.setSpecialty(user.getDoctor().getSpecialty());
        doctor.setDoy(user.getDoctor().getDoy());
        doctor.setAfm(user.getDoctor().getAfm());

        handleDoctorOpeningHoursUpdate(doctor, user.getDoctor().getOpeningHours());

        if (user.getRegisterRequest() != null) {
            handleRegisterRequestUpdate(the_user, user.getRegisterRequest());
        }

        doctorRepository.save(doctor);
        the_user.setDoctor(doctor);
    }

    private void handleDiagnosticCenterUpdate(User the_user, User user) {
        DiagnosticCenter diagnostic = the_user.getDiagnosticCenter();
        if (diagnostic == null) diagnostic = new DiagnosticCenter();

        diagnostic.setAddress(user.getDiagnosticCenter().getAddress());
        diagnostic.setCity(user.getDiagnosticCenter().getCity());
        diagnostic.setState(user.getDiagnosticCenter().getState());
        diagnostic.setAfm(user.getDiagnosticCenter().getAfm());

        if (user.getDiagnosticCenter().getSpecialties() != null) {
            diagnostic.setSpecialties(user.getDiagnosticCenter().getSpecialties());
        }

        handleDiagnosticCenterOpeningHoursUpdate(diagnostic, user.getDiagnosticCenter().getOpeningHours());

        if (user.getRegisterRequest() != null) {
            handleRegisterRequestUpdate(the_user, user.getRegisterRequest());
        }

        diagnosticRepository.save(diagnostic);
        the_user.setDiagnosticCenter(diagnostic);
    }

    private void handleDoctorOpeningHoursUpdate(Doctor doctor, List<OpeningHours> openingHours) {
        if (openingHours == null) return;

        List<OpeningHours> existingOpeningHours = doctor.getOpeningHours();
        Map<Integer, OpeningHours> existingOpeningHoursMap = existingOpeningHours.stream()
                .filter(schedule -> schedule.getId() != null)
                .collect(Collectors.toMap(OpeningHours::getId, schedule -> schedule));

        Set<Integer> processedIds = new HashSet<>();
        List<OpeningHours> updatedOpeningHours = new ArrayList<>();


        for (OpeningHours schedule : openingHours) {
            if (schedule.getId() != null && existingOpeningHoursMap.containsKey(schedule.getId())) {
                OpeningHours existingOpeningHour = existingOpeningHoursMap.get(schedule.getId());
                existingOpeningHour.setDayOfWeek(schedule.getDayOfWeek());
                existingOpeningHour.setStartTime(schedule.getStartTime());
                existingOpeningHour.setEndTime(schedule.getEndTime());
                processedIds.add(schedule.getId());
                updatedOpeningHours.add(existingOpeningHour);
            } else {
                schedule.setDoctor(doctor);
                OpeningHours savedSchedule = openingHoursRepository.save(schedule);
                doctor.getOpeningHours().add(savedSchedule);
                processedIds.add(savedSchedule.getId());
                updatedOpeningHours.add(savedSchedule);
            }
        }

        for (OpeningHours existingOpeningHour : existingOpeningHours) {
            if (existingOpeningHour.getId() != null && !processedIds.contains(existingOpeningHour.getId())) {
                openingHoursRepository.delete(existingOpeningHour); // Delete from database
            }
        }
        doctor.setOpeningHours(updatedOpeningHours);
    }

    private void handleDiagnosticCenterOpeningHoursUpdate(DiagnosticCenter diagnostic, List<OpeningHours> openingHours) {
        if (openingHours == null) return;

        List<OpeningHours> existingOpeningHours = diagnostic.getOpeningHours();
        Map<Integer, OpeningHours> existingOpeningHoursMap = existingOpeningHours.stream()
                .filter(openingHour -> openingHour.getId() != null)
                .collect(Collectors.toMap(OpeningHours::getId, openingHour -> openingHour));

        Set<Integer> processedIds = new HashSet<>();

        List<OpeningHours> updatedOpeningHours = new ArrayList<>();

        for (OpeningHours openingHour : openingHours) {
            if (openingHour.getId() != null && existingOpeningHoursMap.containsKey(openingHour.getId())) {
                // Update existing entry
                OpeningHours existingOpeningHour = existingOpeningHoursMap.get(openingHour.getId());
                existingOpeningHour.setDayOfWeek(openingHour.getDayOfWeek());
                existingOpeningHour.setStartTime(openingHour.getStartTime());
                existingOpeningHour.setEndTime(openingHour.getEndTime());
                processedIds.add(openingHour.getId());
                updatedOpeningHours.add(existingOpeningHour);
            } else {
                // Insert new entry
                openingHour.setDiagnosticCenter(diagnostic);
                OpeningHours savedOpeningHour = openingHoursRepository.save(openingHour);
                processedIds.add(savedOpeningHour.getId());
                updatedOpeningHours.add(savedOpeningHour);
            }
        }

        // Remove unprocessed entries (deletions)
        for (OpeningHours existingOpeningHour : existingOpeningHours) {
            if (existingOpeningHour.getId() != null && !processedIds.contains(existingOpeningHour.getId())) {
                openingHoursRepository.delete(existingOpeningHour); // Delete from database
            }
        }
        diagnostic.setOpeningHours(updatedOpeningHours);
    }


    private void handleRegisterRequestUpdate(User user, RegisterRequest registerRequest) {
        if (registerRequest.getId() != null) {
            // Fetch existing request by ID
            RegisterRequest existingRequest = registerRequestRepository.findById(registerRequest.getId()).orElse(null);
            if (existingRequest != null) {
                existingRequest.setStatus(RegisterRequest.Status.PENDING);
                registerRequestRepository.save(existingRequest);
                user.setRegisterRequest(existingRequest);
                return;
            }
        }

        // If no existing request found, check by userId
        Optional<RegisterRequest> existingRequestByUser = registerRequestRepository.findByUserId(user.getId());
        if (existingRequestByUser.isPresent()) {
            RegisterRequest existingRequest = existingRequestByUser.get();
            existingRequest.setStatus(RegisterRequest.Status.PENDING);
            registerRequestRepository.save(existingRequest);
            user.setRegisterRequest(existingRequest);
        } else {
            // Create a new request
            registerRequest.setStatus(RegisterRequest.Status.PENDING);
            registerRequest.setUser(user);
            registerRequestRepository.save(registerRequest);
            user.setRegisterRequest(registerRequest);
        }
    }

}
