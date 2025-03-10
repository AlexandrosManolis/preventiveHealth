package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import gr.hua.dit.preventiveHealth.entity.users.Role;
import gr.hua.dit.preventiveHealth.entity.users.Specialties;
import gr.hua.dit.preventiveHealth.entity.users.User;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RoleRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.SpecialtiesRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
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
        List<Specialties> specialtiesList = Arrays.asList(
                // REQUIRED specialists (alphabetically ordered)
                new Specialties("Cardiologist", Specialties.RecommendCheckUp.REQUIRED, "Heart checkup, Blood Pressure Check", Specialties.Gender.BOTH, 6, null, 60),
                new Specialties("Dentist", Specialties.RecommendCheckUp.REQUIRED, "Dental Health Check", Specialties.Gender.BOTH, 1, null, 12),
                new Specialties("Dermatologist", Specialties.RecommendCheckUp.REQUIRED, "Skin Cancer Screening, Mole Check (ABCD method), Acne, Wart treatment", Specialties.Gender.BOTH, 12, null, 12),
                new Specialties("Endocrinologist", Specialties.RecommendCheckUp.REQUIRED, "Blood Sugar Test, Thyroid Function Test", Specialties.Gender.BOTH, 6, null, 12),
                new Specialties("Gastroenterologist", Specialties.RecommendCheckUp.REQUIRED, "Colonoscopy, Sigmoidoscopy", Specialties.Gender.BOTH, 50, null, 120),
                new Specialties("Gynecologist", Specialties.RecommendCheckUp.REQUIRED, "Pap Smear, Clinical Breast Exam", Specialties.Gender.FEMALE, 21, null, 12),
                new Specialties("Hematologist", Specialties.RecommendCheckUp.REQUIRED, "Complete Blood Count (CBC), Blood Test, CRP", Specialties.Gender.BOTH, 0, null, 24),
                new Specialties("Laboratory Medicine Specialist", Specialties.RecommendCheckUp.REQUIRED, "Blood and Urine Tests", Specialties.Gender.BOTH, 0, null, 24),
                new Specialties("Mastologist", Specialties.RecommendCheckUp.REQUIRED, "Mammography", Specialties.Gender.FEMALE, 40, null, 12),
                new Specialties("Nephrologist", Specialties.RecommendCheckUp.REQUIRED, "Kidney Function Test, Urinalysis", Specialties.Gender.BOTH, 0, null, 12),
                new Specialties("Ophthalmologist", Specialties.RecommendCheckUp.REQUIRED, "Eye Exam", Specialties.Gender.BOTH, 1, null, 24),
                new Specialties("Pathologist", Specialties.RecommendCheckUp.REQUIRED, "Annual Physical and Clinical Examination", Specialties.Gender.BOTH, 18, null, 12),
                new Specialties("Pediatrician", Specialties.RecommendCheckUp.REQUIRED, "Child Growth and Clinical Examination", Specialties.Gender.BOTH, 0, 18, 12),
                new Specialties("Pneumonologist", Specialties.RecommendCheckUp.REQUIRED, "Chest X-ray, Spirometry for Smokers", Specialties.Gender.BOTH, 35, null, 12),
                new Specialties("Radiologist", Specialties.RecommendCheckUp.REQUIRED, "Chest X-ray", Specialties.Gender.BOTH, 45, null, 24),
                new Specialties("Urologist", Specialties.RecommendCheckUp.REQUIRED, "Prostate Exam, PSA Test", Specialties.Gender.MALE, 50, null, 12),

                // OPTIONAL specialists (alphabetically ordered)
                new Specialties("Allergist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Anesthesiologist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Child Psychiatrist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Emergency Medicine Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("ENT Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Family Medicine Physician", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("General Surgeon", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Geriatrician", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Immunologist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Infectious Disease Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Neurologist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Neurosurgeon", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Obstetrician", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Occupational Medicine Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Oncologist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Orthopedic Surgeon", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Physiatrist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Plastic Surgeon", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Podiatrist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Psychiatrist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Public Health and Preventive Medicine Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Rheumatologist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Sports Medicine Specialist", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Thoracic Surgeon", Specialties.RecommendCheckUp.OPTIONAL),
                new Specialties("Vascular Surgeon", Specialties.RecommendCheckUp.OPTIONAL)
        );
        List<String> existingSpecialties = specialtiesRepository.findAll()
                .stream()
                .map(s -> s.getName().toLowerCase())
                .toList();

        specialtiesList.stream()
                .filter(specialty -> !existingSpecialties.contains(specialty.getName().toLowerCase()))
                .forEach(specialtiesRepository::save);
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
