package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.users.*;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RegisterRequestRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.*;

@Repository
public class UserDAOImpl implements UserDAO{

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RegisterRequestRepository registerRequestRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public UserDAOImpl(UserRepository userRepository, RoleRepository roleRepository, RegisterRequestRepository registerRequestRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.registerRequestRepository = registerRequestRepository;
    }

    //get user profile
    @Override
    public User getUserProfile(Integer user_id) {
        return entityManager.find(User.class, user_id);
    }

    //get user id via username using Query
    @Override
    public Integer getUserId(String username) {
        try {
            Integer userId = entityManager.createQuery(
                            "SELECT u.id FROM User u WHERE u.username = :username", Integer.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return userId;
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public Double averageSpecialistRating(Integer userId) {
        try {
            Double allSpecialistsRatedByUser = entityManager.createQuery(
                            "SELECT AVG(r.rating) FROM RatingSpecialist r WHERE r.doctor.id = :userId OR r.diagnosticCenter.id = :userId", Double.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            return allSpecialistsRatedByUser;
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public List<String> getAllSpecialties() {
        try {
            List<String> specialties = entityManager.createQuery(
                            "SELECT DISTINCT d.specialty " +
                                    "FROM Doctor d JOIN d.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status " +
                                    "UNION " +
                                    "SELECT DISTINCT c.specialties " +
                                    "FROM DiagnosticCenter c JOIN c.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status", String.class)
                    .setParameter("status", RegisterRequest.Status.ACCEPTED)
                    .getResultList();
            return specialties;
        } catch (NoResultException ex) {
            return Collections.emptyList(); // Return an empty list instead of null
        }
    }

    @Override
    public List<String> getAllCities() {
        try {
            List<String> cities = entityManager.createQuery(
                            "SELECT DISTINCT d.city " +
                                    "FROM Doctor d JOIN d.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status " +
                                    "UNION " +
                                    "SELECT DISTINCT c.city " +
                                    "FROM DiagnosticCenter c JOIN c.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status", String.class)
                    .setParameter("status", RegisterRequest.Status.ACCEPTED)
                    .getResultList();
            return cities;
        } catch (NoResultException ex) {
            return Collections.emptyList(); // Return an empty list instead of null
        }
    }

    @Transactional
    public User createDoctorUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            // Create and save user first
            User user = new User(username, this.passwordEncoder.encode("user2!"), "phd2@hua.gr", "User2", "+306999999999");

            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByRoleName("ROLE_DOCTOR")
                    .orElseThrow(() -> new RuntimeException("Doctor role not found")));
            user.setRoles(roles);

            // Save user to generate ID
            user = userRepository.save(user);

            userRepository.flush();

            // Create doctor - the @MapsId will use the user's ID
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setAddress("Pantou 13");
            doctor.setCity("Kallithea");
            doctor.setState("Attica");
            doctor.setDoy("Athens");
            doctor.setSpecialty("Cardiologist");
            doctor.setAfm("235674569");

            // Create opening hours
            List<OpeningHours> openingHours = new ArrayList<>();
            OpeningHours openingHour1 = new OpeningHours(DayOfWeek.MONDAY, "16:30", "20:30");
            OpeningHours openingHour2 = new OpeningHours(DayOfWeek.TUESDAY, "12:30", "20:30");

            openingHour1.setDoctor(doctor);
            openingHour2.setDoctor(doctor);
            openingHours.add(openingHour1);
            openingHours.add(openingHour2);

            doctor.setOpeningHours(openingHours);

            user.setDoctor(doctor);
            user = userRepository.save(user);

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUser(user);
            registerRequest.setStatus(RegisterRequest.Status.ACCEPTED);
            registerRequestRepository.save(registerRequest);

            return null;
        });
    }


    //save user
    @Transactional
    public void saveUser(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
    }

}
