package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Patient;
import gr.hua.dit.preventiveHealth.entity.Role;
import gr.hua.dit.preventiveHealth.entity.User;
import gr.hua.dit.preventiveHealth.repository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InitialDataService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    public InitialDataService(UserRepository userRepository,
                              UserDAO userDAO,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDAO=userDAO;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    //get current date
    private Date getCurrentDate() {

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


        try {
            String formattedDate = dateFormat.format(currentDate);
            return dateFormat.parse(formattedDate);
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception as needed
            return null;
        }
    }

    //create users in db if they are not exist
    private void createRolesUsers() {

        final List<String> rolesToCreate = List.of("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_DIAGNOSTIC", "ROLE_PATIENT");
        for (final String roleName : rolesToCreate) {
            roleRepository.findByRoleName(roleName).orElseGet(() -> {
                roleRepository.save(new Role(roleName));
                return null;
            });
        }

        userRepository.findByUsername("admin").orElseGet(()-> {

            User adminUser = new User("admin", this.passwordEncoder.encode("admin"),"admin@example.com","Admin","+306912345678");
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow(()-> new RuntimeException("Admin role not found")));
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            return null;
        });

        userRepository.findByUsername("user1").orElseGet(()-> {

            User user = new User("user1", this.passwordEncoder.encode("user1"),"user1@example.com","User1","+306923456781");
            Patient patient = new Patient(user,Patient.Gender.MALE, "23/05/1998", "23059812345");
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByRoleName("ROLE_PATIENT").orElseThrow(()-> new RuntimeException("Patient role not found")));
            user.setPatient(patient);
            user.setRoles(roles);

            userRepository.save(user);
            return null;
        });

    }
    //when program starts call createRolesUsers
    @PostConstruct
    public void setup(){
        this.createRolesUsers();
    }
}
