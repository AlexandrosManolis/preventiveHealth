package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.config.JwtUtils;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.payload.request.DiagnosticSignupRequest;
import gr.hua.dit.preventiveHealth.payload.request.DoctorSignupRequest;
import gr.hua.dit.preventiveHealth.payload.request.LoginRequest;
import gr.hua.dit.preventiveHealth.payload.request.PatientSignupRequest;
import gr.hua.dit.preventiveHealth.payload.response.JwtResponse;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import gr.hua.dit.preventiveHealth.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    //create a new user
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
        if (userRepository.existsByAmka(signupRequest.getAmka())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhone());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("ROLE_PATIENT")
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
        if (userRepository.existsByAfm(signupRequest.getAfm())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhone());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("ROLE_DOCTOR")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
        user.setRoles(roles);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setAddress(signupRequest.getAddress());
        doctor.setAfm(signupRequest.getAfm());
        doctor.setDoy(signupRequest.getDoy());
        doctor.setState(signupRequest.getState());
        doctor.setSpecialty(signupRequest.getSpecialty());
        doctor.setSchedules(signupRequest.getSchedules());

        user.setDoctor(doctor);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUser(user);
        registerRequest.setStatus(RegisterRequest.Status.PENDING);

        //save user
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

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
        if (userRepository.existsByAfm(signupRequest.getAfm())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Afm is already in use!"));
        }

        // Create new user's account
        User user= new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()), signupRequest.getEmail(), signupRequest.getFullName(),signupRequest.getPhone());

        //set role farmer
        Set<Role> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("ROLE_DIAGNOSTIC")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
        user.setRoles(roles);

        DiagnosticCenter diagnosticCenter = new DiagnosticCenter();
        diagnosticCenter.setUser(user);
        diagnosticCenter.setAddress(signupRequest.getAddress());
        diagnosticCenter.setAfm(signupRequest.getAfm());
        diagnosticCenter.setDoy(signupRequest.getDoy());
        diagnosticCenter.setState(signupRequest.getState());
        diagnosticCenter.setSpecialties(signupRequest.getSpecialties());
        diagnosticCenter.setSchedules(signupRequest.getSchedules());

        user.setDiagnosticCenter(diagnosticCenter);

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUser(user);
        registerRequest.setStatus(RegisterRequest.Status.PENDING);

        //save user
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
