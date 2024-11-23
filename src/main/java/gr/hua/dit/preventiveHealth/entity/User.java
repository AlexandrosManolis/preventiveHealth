package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "username"),
            @UniqueConstraint(columnNames = "email")
        }
)
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

    @NotBlank
    private String phone;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private Patient patient;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL)
    private Doctor doctor;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DiagnosticCenter diagnosticCenter;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = {@JoinColumn(name = "userId"), @JoinColumn(name = "roleId")})
    private Set<Role> roles= new HashSet<>();


    public User() {
    }

    public User(String username, String password, String email, String fullName, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.roles = new HashSet<>();
    }

    public User(String username, String password, String email, String fullName, String phone, Patient patient) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.patient = patient;
        this.roles = new HashSet<>();

        if (patient != null) {
            patient.setUser(this); // Ensure bidirectional relationship
        }
    }

    public User(String username, String password, String email, String fullName, String phone, Doctor doctor) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.doctor = doctor;
        this.roles = new HashSet<>();

        if(doctor != null) {
            doctor.setUser(this);
        }
    }

    public User(String username, String password, String email, String fullName, String phone, DiagnosticCenter diagnosticCenter) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
