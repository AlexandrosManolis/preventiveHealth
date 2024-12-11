package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Doctor;
import gr.hua.dit.preventiveHealth.entity.Patient;
import gr.hua.dit.preventiveHealth.entity.Schedule;
import gr.hua.dit.preventiveHealth.entity.User;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import gr.hua.dit.preventiveHealth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestController{

    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("{userId}/profile")
    public ResponseEntity<?> userProfile(@PathVariable Integer userId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get authenticated username
        String userRole = userService.getUserRole(); // Get authenticated user's role
        Integer authUserId = userDAO.getUserId(username); // Fetch authenticated user's ID

        User user = userDAO.getUserProfile(userId);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND); // Return 404 if user does not exist
        }

        boolean isAdmin = "ROLE_ADMIN".equals(userRole);
        boolean isOwner = userId.equals(authUserId);

        if (!isOwner && !isAdmin) {
            return new ResponseEntity<>("Unauthorized to access this profile", HttpStatus.UNAUTHORIZED);
        }
        user.setPassword(null);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("edit/{userId}")
    public ResponseEntity<?> editUser(@PathVariable Integer userId) {
        User existingUser = userService.getUserProfile(userId);

        if (existingUser == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        return new ResponseEntity<>(existingUser, HttpStatus.OK);
    }

    @PostMapping("edit/{userId}")
    public ResponseEntity<?> addUser(@PathVariable Integer userId, @RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userRole = userService.getUserRole();

        String username = authentication.getName();
        Integer authUserId = userDAO.getUserId(username);
        User edited_user = userService.getUserProfile(userId);

        edited_user.setUsername(user.getUsername());
        edited_user.setEmail(user.getEmail());
        edited_user.setFullName(user.getFullName());
        edited_user.setPhoneNumber(user.getPhoneNumber());

        if(edited_user.getRoles().stream().anyMatch(role -> "ROLE_PATIENT".equals(role.getRoleName()))){
            Patient patient = edited_user.getPatient();
            patient.setUser(edited_user);
            patient.setAmka(user.getPatient().getAmka());
            patient.setGender(user.getPatient().getGender());
            patient.setBirthday(user.getPatient().getBirthday());

            edited_user.setPatient(patient);
        } else if (edited_user.getRoles().stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getRoleName()))) {
            Doctor doctor = edited_user.getDoctor();
            for(int i= 0; i< edited_user.getDoctor().getSchedules().size(); i++){

            }
            doctor.setSchedules(user.getDoctor().getSchedules());
        }
        userRepository.save(edited_user);
        return new ResponseEntity<>(edited_user, HttpStatus.OK);
    }
}
