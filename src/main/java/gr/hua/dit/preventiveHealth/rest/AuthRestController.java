package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.config.JwtUtils;
import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.payload.request.DiagnosticSignupRequest;
import gr.hua.dit.preventiveHealth.payload.request.DoctorSignupRequest;
import gr.hua.dit.preventiveHealth.payload.request.LoginRequest;
import gr.hua.dit.preventiveHealth.payload.request.PatientSignupRequest;
import gr.hua.dit.preventiveHealth.payload.response.JwtResponse;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.payload.validation.Create;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RegisterRequestRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import gr.hua.dit.preventiveHealth.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RegisterRequestRepository registerRequestRepository;

    @Autowired
    private JwtUtils jwtUtils;

    //check username and password and if they are right set token and enter the platform
    @PostMapping("signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("authentication");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        System.out.println("authentication: " + authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("post authentication");
        String jwt = jwtUtils.generateJwtToken(authentication);
        System.out.println("jw: " + jwt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @GetMapping("/check-user")
    public ResponseEntity<Map<String, Boolean>> checkUser(@RequestParam(required = false) String username,
                                                          @RequestParam(required = false) String email) {
        boolean emailExists = userRepository.existsByEmail(email);
        boolean usernameExists = userRepository.existsByUsername(username);
        boolean userExists = emailExists || usernameExists;
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", userExists);
        return ResponseEntity.ok(response);
    }

    //create a new user
    @Validated(Create.class)
    @PostMapping("signup/patient")
    public ResponseEntity<?> registerUser(@Valid @RequestBody PatientSignupRequest signupRequest) {
        //check if user's data exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByPatient_Amka(signupRequest.getAmka())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhoneNumber());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName("ROLE_PATIENT")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }

        user.setRoles(roles);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setAmka(signupRequest.getAmka());
        patient.setBirthday(signupRequest.getBirthday());
        patient.setGender(signupRequest.getGender());

        user.setPatient(patient);

        //save user
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Validated(Create.class)
    @PostMapping("signup/doctor")
    public ResponseEntity<?> registerUser(@Valid @RequestBody DoctorSignupRequest signupRequest) {
        //check if user's data exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByDoctor_Afm(signupRequest.getAfm())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhoneNumber());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName("ROLE_DOCTOR")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
        user.setRoles(roles);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setAddress(signupRequest.getAddress());
        doctor.setCity(signupRequest.getCity());
        doctor.setAfm(signupRequest.getAfm());
        doctor.setDoy(signupRequest.getDoy());
        doctor.setState(signupRequest.getState());
        doctor.setSpecialty(signupRequest.getSpecialty());

        List<OpeningHours> openingHours = signupRequest.getOpeningHours().stream()
                .map(openingHourRequest -> {
                    OpeningHours openingHour = new OpeningHours();
                    openingHour.setDayOfWeek(openingHourRequest.getDayOfWeek());
                    openingHour.setStartTime(openingHourRequest.getStartTime());
                    openingHour.setEndTime(openingHourRequest.getEndTime());
                    openingHour.setDoctor(doctor);
                    return openingHour;
                })
                .collect(Collectors.toList());

        doctor.setOpeningHours(openingHours);

        user.setDoctor(doctor);

        //save user
        userRepository.save(user);

        if (registerRequestRepository.existsByUser(user)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User already has an active request!"));
        }
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUser(user);
        registerRequest.setStatus(RegisterRequest.Status.PENDING);

        registerRequestRepository.save(registerRequest);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @Validated(Create.class)
    @PostMapping("signup/diagnostic")
    public ResponseEntity<?> registerUser(@Valid @RequestBody DiagnosticSignupRequest signupRequest) {
        //check if user's data exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        if (userRepository.existsByDiagnosticCenter_Afm(signupRequest.getAfm())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhoneNumber());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName("ROLE_DIAGNOSTIC")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
        user.setRoles(roles);

        DiagnosticCenter diagnosticCenter = new DiagnosticCenter();
        diagnosticCenter.setUser(user);
        diagnosticCenter.setAddress(signupRequest.getAddress());
        diagnosticCenter.setCity(signupRequest.getCity());
        diagnosticCenter.setAfm(signupRequest.getAfm());
        diagnosticCenter.setDoy(signupRequest.getDoy());
        diagnosticCenter.setState(signupRequest.getState());
        diagnosticCenter.setSpecialties(signupRequest.getSpecialties());

        List<OpeningHours> openingHours = signupRequest.getOpeningHours().stream()
                .map(openingHourRequest -> {
                    OpeningHours openingHour = new OpeningHours();
                    openingHour.setDayOfWeek(openingHourRequest.getDayOfWeek());
                    openingHour.setStartTime(openingHourRequest.getStartTime());
                    openingHour.setEndTime(openingHourRequest.getEndTime());
                    openingHour.setDiagnosticCenter(diagnosticCenter);

                    return openingHour;
                })
                .collect(Collectors.toList());

        diagnosticCenter.setOpeningHours(openingHours);

        user.setDiagnosticCenter(diagnosticCenter);

        //save user
        userRepository.save(user);

        if (registerRequestRepository.existsByUser(user)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User already has an active request!"));
        }
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUser(user);
        registerRequest.setStatus(RegisterRequest.Status.PENDING);

        registerRequestRepository.save(registerRequest);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
