package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.SpecialtiesRepository;
import gr.hua.dit.preventiveHealth.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

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
    private SpecialtiesRepository specialtiesRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

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

    private void addSpecialties() {
        List<String> specialties = Arrays.asList(
                "Allergist", "Anesthesiologist", "Cardiologist", "Dermatologist", "Emergency Medicine Specialist",
                "Endocrinologist", "Family Medicine Physician", "Gastroenterologist", "General Surgeon",
                "Geriatrician", "Hematologist", "Infectious Disease Specialist", "Internist", "Nephrologist",
                "Neurologist", "Neurosurgeon", "Obstetrician", "Gynecologist", "Oncologist", "Ophthalmologist",
                "Orthopedic Surgeon", "ENT Specialist", "Pathologist", "Pediatrician", "Physiatrist",
                "Plastic Surgeon", "Psychiatrist", "Pulmonologist", "Radiologist", "Rheumatologist",
                "Sports Medicine Specialist", "Thoracic Surgeon", "Urologist", "Vascular Surgeon", "Anaesthesiologist",
                "Biological Hematologist", "Child Psychiatrist", "Clinical Biologist", "Clinical Chemist",
                "Clinical Neurophysiologist", "Clinical Radiologist", "Oral and Maxillo-Facial Surgeon", "Dermatologist",
                "General Hematologist", "General Practitioner", "Immunologist", "Laboratory Medicine Specialist",
                "Microbiologist", "Neuro-Psychiatrist", "Nuclear Medicine Specialist", "Occupational Medicine Specialist",
                "Otorhinolaryngologist", "Pediatric Surgeon", "Pharmacologist", "Respiratory Medicine Specialist",
                "Physical Medicine and Rehabilitation Specialist", "Tropical Medicine Specialist", "Venereologist",
                "Podiatrist", "Public Health and Preventive Medicine Specialist", "Radiotherapist", "Stomatologist"
        );
        List<String> existingSpecialties = specialtiesRepository.findAll()
                .stream()
                .map(s -> s.getName().toLowerCase())
                .toList();

        specialties.stream()
                .filter(specialty -> !existingSpecialties.contains(specialty.toLowerCase()))
                .forEach(specialty -> specialtiesRepository.save(new Specialties(specialty)));
    }

    private void everyDayCheckAppointments() {
        List<Appointment> appointments = appointmentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Appointment appointment : appointments) {

            if((appointment.getDate().isBefore(today) && appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.PENDING)
                || (appointment.getDate().isBefore(today.minusDays(7)) && appointment.getAppointmentStatus() == Appointment.AppointmentStatus.PENDING && appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED)) {
                appointment.setAppointmentRequestStatus(Appointment.AppointmentRequestStatus.REJECTED);
                appointment.setAppointmentStatus(Appointment.AppointmentStatus.CANCELLED);
                appointment.setRejectionCause("Rejected due to the appointment's date has already passed.");
                appointmentRepository.save(appointment);
            }

            if(appointment.getDate().isBefore(today.minusMonths(1)) && (appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.REJECTED || appointment.getAppointmentStatus() == Appointment.AppointmentStatus.CANCELLED)) {
                appointmentRepository.delete(appointment);
            }
        }
    }

    //when program starts call functions
    @PostConstruct
    public void setup() {
        this.createRolesUsers();
        this.addSpecialties();
        this.everyDayCheckAppointments();
    }
}
