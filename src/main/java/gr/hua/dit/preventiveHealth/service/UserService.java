package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.repository.usersRepository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    //get user with a specific id
    public Object getUser(Integer userId) {
        return userRepository.findById(userId).get();
    }

    //get user's role
    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getAuthorities() != null) {
            if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
                return "ROLE_ADMIN";
            } else if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_DOCTOR"))) {
                return "ROLE_DOCTOR";
            } else if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_DIAGNOSTIC"))) {
                return "ROLE_DIAGNOSTIC";
            }else if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_PATIENT"))) {
                return "ROLE_PATIENT";
            }
        }
        // Default role if no matching role is found
        return "ROLE_USER";
    }
}
