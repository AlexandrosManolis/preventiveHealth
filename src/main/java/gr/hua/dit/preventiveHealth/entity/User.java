package gr.hua.dit.preventiveHealth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "username"),
            @UniqueConstraint(columnNames = "email")
        }
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 20)
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;

    @NotNull
    private String phoneNumber;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Patient patient;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Doctor doctor;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private DiagnosticCenter diagnosticCenter;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "roleId")})
    private Set<Role> roles= new HashSet<>();

    @OneToMany(mappedBy="user", cascade = CascadeType.ALL)
    private List<RegisterRequest> registerRequests;
    public List<RegisterRequest> getRegisterRequests() {
        return registerRequests;
    }

    public User() {
    }

    public User(String username, String password, String email, String fullName, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, String email, String fullName, String phoneNumber, Patient patient) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.patient = patient;
        this.roles = new HashSet<>();

        if (patient != null) {
            patient.setUser(this); // Ensure bidirectional relationship
        }
    }

    public User(String username, String password, String email, String fullName, String phoneNumber, Doctor doctor) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.doctor = doctor;
        this.roles = new HashSet<>();

        if(doctor != null) {
            doctor.setUser(this);
        }
    }

    public User(String username, String password, String email, String fullName, String phoneNumber, DiagnosticCenter diagnosticCenter) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.diagnosticCenter = diagnosticCenter;
        this.roles = new HashSet<>();

        if(diagnosticCenter != null) {
            diagnosticCenter.setUser(this);
        }
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public DiagnosticCenter getDiagnosticCenter() {
        return diagnosticCenter;
    }

    public void setDiagnosticCenter(DiagnosticCenter diagnosticCenter) {
        this.diagnosticCenter = diagnosticCenter;
    }

    @Override
    public String toString() {
        return fullName;
    }

    public Integer getId() { return id; }

    public void setId(int id) { this.id = id; }
}
