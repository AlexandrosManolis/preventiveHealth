package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.*;
import gr.hua.dit.preventiveHealth.repository.usersRepository.*;
import gr.hua.dit.preventiveHealth.service.GmailService;
import gr.hua.dit.preventiveHealth.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.preventiveHealth.payload.validation.Update;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;


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

    @Autowired
    private RatingSpecialistRepository ratingSpecialistRepository;

    @Autowired
    private GmailService gmailService;

    @GetMapping("/get-logo")
    public ResponseEntity<Resource> getLogo(){
        ClassPathResource imgFile = new ClassPathResource("static/home-image.webp");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("image/webp"));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(imgFile);
    }


    @GetMapping("find_specialist")
    public ResponseEntity<?> getAllSpecialties(@RequestParam (required = false) String specialty, @RequestParam (required = false) String speciality_min, @RequestParam (required = false) String city) {
        List<User> allSpecialists = new ArrayList<>();
        List<Doctor> doctors = new ArrayList<>();
        List<DiagnosticCenter> diagnostics = new ArrayList<>();


        if(specialty == null || city == null) {
            List<String> allSpecialties = userDAO.getAllSpecialties();
            List<String> allcities = userDAO.getAllCities();
            Map<String, List> cityAndSpecialty = new HashMap<>();
            cityAndSpecialty.put("allSpecialties", allSpecialties);
            cityAndSpecialty.put("allcities", allcities);
            return new ResponseEntity<>(cityAndSpecialty, HttpStatus.OK);
        }else if (specialty.equals("all") && city.equals("all")) {
            doctors = doctorRepository.findAll();
            diagnostics = diagnosticRepository.findAll();
        } else if (!specialty.equals("all") && city.equals("all")) {
            diagnostics = diagnosticRepository.findBySpecialties(specialty);
            doctors = doctorRepository.findBySpecialty(specialty);
        } else if (specialty.equals("all") && !city.equals("all")) {
            diagnostics = diagnosticRepository.findByCity(city);
            doctors = doctorRepository.findByCity(city);
        } else {
            List<Doctor> doctorsBySpecialty;
            List<DiagnosticCenter> diagnosticsBySpecialty;

            diagnosticsBySpecialty = diagnosticRepository.findBySpecialties(specialty);
            doctorsBySpecialty = doctorRepository.findBySpecialty(specialty);

            for (Doctor doctor : doctorsBySpecialty) {
                if(doctor.getCity().equals(city)) {
                    doctors.add(doctor);
                }
            }
            for (DiagnosticCenter diagnostic : diagnosticsBySpecialty) {
                if(diagnostic.getCity().equals(city)) {
                    diagnostics.add(diagnostic);
                }
            }
        }

        for (Doctor doctor : doctors) {
            RegisterRequest rr = doctor.getUser().getRegisterRequest();
            if (rr != null && rr.getStatus() == RegisterRequest.Status.ACCEPTED) {
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
            RegisterRequest rr = diagnostic.getUser().getRegisterRequest();
            if (rr != null && rr.getStatus() == RegisterRequest.Status.ACCEPTED) {
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

    @GetMapping("specialist/{userId}/allRatings")
    public ResponseEntity<?> allRatings(@PathVariable Integer userId){
        User user = userDAO.getUserProfile(userId);
        String userRole = user.getRoles().stream().findFirst().map(Role :: getRoleName).orElse("No role found");
        List<RatingSpecialist> ratingSpecialist;

        if(userRole.equals("ROLE_DIAGNOSTIC")){
            ratingSpecialist = ratingSpecialistRepository.getRatingSpecialistByDiagnosticCenterId(userId);
        } else if (userRole.equals("ROLE_DOCTOR")) {
            ratingSpecialist = ratingSpecialistRepository.getRatingSpecialistByDoctorId(userId);
        }else{
            return ResponseEntity.badRequest().body("User provided is not authorized for ratings");
        }

        List<Map<String,Object>> allRatings = ratingSpecialist.stream().map(rating -> {
            Map<String,Object> filteredRating = new HashMap<>();
            filteredRating.put("rating", rating.getRating());
            filteredRating.put("ratingDescription", rating.getRatingDescription());
            filteredRating.put("patientFullName", rating.getPatient().getFullName());
            if(userRole.equals("ROLE_DOCTOR")){
                filteredRating.put("doctorFullName", rating.getDoctor().getFullName());
                filteredRating.put("doctorAddress", rating.getDoctor().getAddress()+ rating.getDoctor().getCity() + rating.getDoctor().getState());
            }else {
                filteredRating.put("diagnosticFullName", rating.getDiagnosticCenter().getFullName());
                filteredRating.put("diagnosticAddress", rating.getDiagnosticCenter().getAddress()+ ", " + rating.getDiagnosticCenter().getCity() + ", " + rating.getDiagnosticCenter().getState());
            }
            return filteredRating;
        }).toList();
        return ResponseEntity.ok(allRatings);
    }

    @PostMapping("specialist/{userId}/rating")
    public ResponseEntity<?> rateSpecialist(@PathVariable Integer userId, @RequestBody RatingSpecialist ratingSpecialist) {
        User user = userDAO.getUserProfile(userId);
        String userRole = user.getRoles().stream().anyMatch(role -> role.equals("ROLE_DOCTOR")) ? "ROLE_DOCTOR" : "ROLE_DIAGNOSTIC";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Integer patientId = userDAO.getUserId(username);

        Patient patient = patientRepository.findById(patientId).orElse(null);
        Doctor doctor = doctorRepository.findById(userId).orElse(null);
        DiagnosticCenter diagnosticCenter = diagnosticRepository.findById(userId).orElse(null);
        Boolean exists;

        if (userRole.equals("ROLE_DOCTOR")) {

            exists = ratingSpecialistRepository.existsByDoctorIdAndPatientId(userId, patientId);
        }else{
            exists = ratingSpecialistRepository.existsByDiagnosticCenterIdAndPatientId(userId, patientId);

        }

        if(exists){
            throw new IllegalArgumentException("You have already rated this doctor.");
        }
        RatingSpecialist newRatingSpecialist = new RatingSpecialist();

        newRatingSpecialist.setRating(ratingSpecialist.getRating());
        newRatingSpecialist.setRatingDescription(ratingSpecialist.getRatingDescription());
        newRatingSpecialist.setPatient(patient);
        newRatingSpecialist.setDoctor(doctor);
        newRatingSpecialist.setDiagnosticCenter(diagnosticCenter);

        ratingSpecialistRepository.save(newRatingSpecialist);

        return new ResponseEntity<>(newRatingSpecialist, HttpStatus.OK);
    }

    @GetMapping("allSpecialties")
    public ResponseEntity<?> getAllSpecialties() {
        List<Specialties> specialties = specialtiesRepository.findAll();
        return ResponseEntity.ok(specialties);
    }

    @GetMapping("specialties")
    public ResponseEntity<?> getSpecialties() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new ResponseEntity<>("Authentication required", HttpStatus.UNAUTHORIZED);
        }

        Integer userId = userDAO.getUserId(username);
        if (userId == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        User user = userDAO.getUserProfile(userId);
        String userRole = userService.getUserRole();

        List<Specialties> specialties = specialtiesRepository.findAll();

        if (user != null && userRole.equals("ROLE_PATIENT")) {
            LocalDate currentDate = LocalDate.now();
            double age = ChronoUnit.DAYS.between(user.getPatient().getBirthday(), currentDate) / 365.25;

            for (int i = specialties.size() - 1; i >= 0; i--) {
                Specialties specialty = specialties.get(i);

                if (specialty.getRecommendCheckUp() == Specialties.RecommendCheckUp.REQUIRED) {

                    if (specialty.getGender() != null && specialty.getGender() != Specialties.Gender.BOTH) {
                        if (user.getPatient().getGender() == null || !specialty.getGender().name().equals(user.getPatient().getGender().name())) {
                            specialties.remove(i);
                            continue;
                        }
                    }

                    if (age < specialty.getMinAge() || (specialty.getMaxAge() != null && age > specialty.getMaxAge())) {
                        specialty.setRecommendCheckUp(Specialties.RecommendCheckUp.OPTIONAL);
                    }
                }
            }
            return ResponseEntity.ok(specialties);
        }else if (user != null && (userRole.equals("ROLE_DOCTOR") || userRole.equals("ROLE_DIAGNOSTIC"))) {
            ArrayList<String> specialtiesList = new ArrayList<>();
            for (Specialties specialty : specialties) {
                specialtiesList.add(specialty.getSpecialty());
            }
            return new ResponseEntity<>(specialtiesList, HttpStatus.OK);
        }
        return new ResponseEntity<>("Specialties not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("specialist/{userId}/rating")
    public ResponseEntity<?> specialistRating(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();

        Double averageSpecialistRating = userDAO.averageSpecialistRating(userId);

        if(averageSpecialistRating == null){
            averageSpecialistRating = 0.0;
        }
        response.put("averageSpecialistRating", averageSpecialistRating);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser") && authentication.getAuthorities().toString().contains("ROLE_PATIENT")) {
            String username = authentication.getName();
            Integer patientId = userDAO.getUserId(username);

            User user = userDAO.getUserProfile(userId);
            String userRole = user.getRoles().stream().anyMatch(role -> role.equals("ROLE_DOCTOR")) ? "ROLE_DOCTOR" : "ROLE_DIAGNOSTIC";
            Boolean exists;
            Integer ratingNumber = null;

            if (userRole.equals("ROLE_DOCTOR")) {

                exists = ratingSpecialistRepository.existsByDoctorIdAndPatientId(userId, patientId);
                if(exists){
                    ratingNumber = ratingSpecialistRepository.findRatingByDoctorIdAndPatientId(userId, patientId);
                }
            }else{
                exists = ratingSpecialistRepository.existsByDiagnosticCenterIdAndPatientId(userId, patientId);
                if(exists){
                    ratingNumber = ratingSpecialistRepository.findRatingByDiagnosticCenterIdAndPatientId(userId, patientId);
                }
            }

            response.put("ratingExists", exists);
            if(exists && ratingNumber != null){
                response.put("patientRating", ratingNumber);
            }
        }

        return ResponseEntity.ok(response);
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
            gmailService.sendEmail(user.getEmail(),"Your account details have been successfully updated.", "Your account details have been successfully updated. If you did not authorize these changes, please contact us immediately.");
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

        doctor.setUser(the_user);

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

        // Set the bidirectional relationship
        diagnostic.setUser(the_user);

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
        if (existingOpeningHours == null) {
            existingOpeningHours = new ArrayList<>();
            doctor.setOpeningHours(existingOpeningHours);
        }

        Map<Integer, OpeningHours> existingOpeningHoursMap = existingOpeningHours.stream()
                .filter(schedule -> schedule.getId() != null)
                .collect(Collectors.toMap(OpeningHours::getId, schedule -> schedule));

        Set<Integer> processedIds = new HashSet<>();

        // Process incoming opening hours
        for (OpeningHours schedule : openingHours) {
            if (schedule.getId() != null && existingOpeningHoursMap.containsKey(schedule.getId())) {
                // Update existing entry
                OpeningHours existingOpeningHour = existingOpeningHoursMap.get(schedule.getId());
                existingOpeningHour.setDayOfWeek(schedule.getDayOfWeek());
                existingOpeningHour.setStartTime(schedule.getStartTime());
                existingOpeningHour.setEndTime(schedule.getEndTime());
                processedIds.add(schedule.getId());
            } else {
                // Add new entry
                schedule.setDoctor(doctor);
                existingOpeningHours.add(schedule);
                if (schedule.getId() != null) {
                    processedIds.add(schedule.getId());
                }
            }
        }

        // Remove unprocessed entries (deletions) - iterate backwards to avoid index issues
        for (int i = existingOpeningHours.size() - 1; i >= 0; i--) {
            OpeningHours existingOpeningHour = existingOpeningHours.get(i);
            if (existingOpeningHour.getId() != null && !processedIds.contains(existingOpeningHour.getId())) {
                existingOpeningHours.remove(i); // This will trigger cascade delete due to orphanRemoval
            }
        }
    }

    private void handleDiagnosticCenterOpeningHoursUpdate(DiagnosticCenter diagnostic, List<OpeningHours> openingHours) {
        if (openingHours == null) return;

        List<OpeningHours> existingOpeningHours = diagnostic.getOpeningHours();
        if (existingOpeningHours == null) {
            existingOpeningHours = new ArrayList<>();
            diagnostic.setOpeningHours(existingOpeningHours);
        }

        Map<Integer, OpeningHours> existingOpeningHoursMap = existingOpeningHours.stream()
                .filter(openingHour -> openingHour.getId() != null)
                .collect(Collectors.toMap(OpeningHours::getId, openingHour -> openingHour));

        Set<Integer> processedIds = new HashSet<>();

        // Process incoming opening hours
        for (OpeningHours openingHour : openingHours) {
            if (openingHour.getId() != null && existingOpeningHoursMap.containsKey(openingHour.getId())) {
                // Update existing entry
                OpeningHours existingOpeningHour = existingOpeningHoursMap.get(openingHour.getId());
                existingOpeningHour.setDayOfWeek(openingHour.getDayOfWeek());
                existingOpeningHour.setStartTime(openingHour.getStartTime());
                existingOpeningHour.setEndTime(openingHour.getEndTime());
                processedIds.add(openingHour.getId());
            } else {
                // Add new entry
                openingHour.setDiagnosticCenter(diagnostic);
                existingOpeningHours.add(openingHour);
                if (openingHour.getId() != null) {
                    processedIds.add(openingHour.getId());
                }
            }
        }

        // Remove unprocessed entries (deletions) - iterate backwards to avoid index issues
        for (int i = existingOpeningHours.size() - 1; i >= 0; i--) {
            OpeningHours existingOpeningHour = existingOpeningHours.get(i);
            if (existingOpeningHour.getId() != null && !processedIds.contains(existingOpeningHour.getId())) {
                existingOpeningHours.remove(i); // This will trigger cascade delete due to orphanRemoval
            }
        }
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
