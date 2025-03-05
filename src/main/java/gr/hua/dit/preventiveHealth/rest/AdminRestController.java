package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.users.RegisterRequest;
import gr.hua.dit.preventiveHealth.entity.users.User;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RegisterRequestRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/admin")
public class AdminRestController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RegisterRequestRepository registerRequestRepository;

    @GetMapping("")
    public ResponseEntity<?> getAllUsers(){
        List<User> users = userRepository.findAll();
        List<User> filteredUsers = new ArrayList<>();

        for (User user : users) {
            user.setPassword(null);
            System.out.println(user.getRoles());
            if (user.getRoles().stream().anyMatch(role -> "ROLE_DOCTOR".equals(role.getRoleName())) || user.getRoles().stream().anyMatch(role -> "ROLE_DIAGNOSTIC".equals(role.getRoleName()))) {
                if(user.getRegisterRequest().getStatus() == RegisterRequest.Status.ACCEPTED){
                    filteredUsers.add(user);
                }
            }else{
                filteredUsers.add(user);
            }
        }
        return new ResponseEntity<>(filteredUsers, HttpStatus.OK);
    }

    @GetMapping("pendingRequests")
    public ResponseEntity<?> getPendingUsers(){
        List<RegisterRequest> requests = registerRequestRepository.findByStatus(RegisterRequest.Status.PENDING);

        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    @PostMapping("pendingRequests/{requestId}/accept")
    public ResponseEntity<?> acceptPendingUser(@PathVariable Integer requestId){
        RegisterRequest request = registerRequestRepository.findById(requestId).orElseThrow(()-> new ResourceNotFoundException("Not exist id: "+requestId));
        if(request.getStatus() == RegisterRequest.Status.PENDING){
            request.setStatus(RegisterRequest.Status.ACCEPTED);
            registerRequestRepository.save(request);
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("User has no pending request"));
        }
        return new ResponseEntity<>(request, HttpStatus.OK);
    }

    @PostMapping("pendingRequests/{requestId}/reject")
    public ResponseEntity<?> rejectPendingUser(@PathVariable Integer requestId, @RequestBody(required = false) String reason ){
        RegisterRequest request = registerRequestRepository.findById(requestId).orElseThrow(()-> new ResourceNotFoundException("Not exist id: "+requestId));
        if(request.getStatus() == RegisterRequest.Status.PENDING){
            request.setStatus(RegisterRequest.Status.REJECTED);
            request.setDescription(reason);
            registerRequestRepository.save(request);
        }else{
            return ResponseEntity.badRequest().body(new MessageResponse("User has no pending request"));
        }
        return new ResponseEntity<>(request, HttpStatus.OK);
    }
}
