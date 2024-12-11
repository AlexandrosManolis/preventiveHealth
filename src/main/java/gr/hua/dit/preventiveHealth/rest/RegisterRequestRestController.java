package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.entity.User;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import gr.hua.dit.preventiveHealth.service.RegisterRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/register-request")
public class RegisterRequestRestController {

    @Autowired
    private RegisterRequestService registerRequestService;

    @Autowired
    private UserRepository userRepository;


    @GetMapping("pending")
    public ResponseEntity<Map<String, Boolean>> pendingRequestExistence(@RequestParam(required = false) String username) {
        // Initialize the response map
        Map<String, Boolean> response = new HashMap<>();

        if (username == null || username.isEmpty()) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }

        // Fetch user by username
        Optional<User> user = userRepository.findByUsername(username);

        // Check if user exists
        if (user.isEmpty()) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }

        Integer userId = user.get().getId();
        boolean pendingRequestExists = registerRequestService.isUserPending(userId);

        response.put("exists", pendingRequestExists);
        System.out.println("pending request: " + pendingRequestExists);


        return ResponseEntity.ok(response);
    }

    @GetMapping("rejected")
    public ResponseEntity<Map<String, Boolean>> rejectedRequestExistence(@RequestParam(required = false) String username) {
        // Initialize the response map
        Map<String, Boolean> response = new HashMap<>();

        if (username == null || username.isEmpty()) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }

        // Fetch user by username
        Optional<User> user = userRepository.findByUsername(username);

        // Check if user exists
        if (user.isEmpty()) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }

        Integer userId = user.get().getId();
        boolean rejectedRequestExists = registerRequestService.isUserRejected(userId);

        response.put("exists", rejectedRequestExists);
        System.out.println("pending request: " + rejectedRequestExists);


        return ResponseEntity.ok(response);
    }
}
