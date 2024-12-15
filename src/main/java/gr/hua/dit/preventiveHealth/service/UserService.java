package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import gr.hua.dit.preventiveHealth.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDAO userDAO;

    @Transactional
    public Integer updateUser(User user) {
        user = userRepository.save(user);
        return user.getId();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    //get users
    @Transactional
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    //get user with a specific id
    public Object getUser(Integer userId) {
        return userRepository.findById(userId).get();
    }

    //get user profile
    public User getUserProfile(Integer user_id) {
        return userDAO.getUserProfile(user_id);
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
