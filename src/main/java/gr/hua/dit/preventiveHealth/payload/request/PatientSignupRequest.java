package gr.hua.dit.preventiveHealth.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import gr.hua.dit.preventiveHealth.entity.Patient;
import gr.hua.dit.preventiveHealth.entity.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Set;

public class PatientSignupRequest {

    public enum Gender {
        MALE,
        FEMALE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private Patient.Gender gender;

    @NotNull
    @Past(message = "Birthday must be in the past") // Example of an additional validation for a past date
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank
    @Size(min = 11, max = 11, message = "Number should contain exactly 11 digits.")
    @Pattern(regexp = "\\d+", message = "Number should contain only digits.")
    private String amka;

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
    private String phoneNumber;

    private Set<Role> roles;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Patient.Gender getGender() {
        return gender;
    }

    public void setGender(Patient.Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAmka() {
        return amka;
    }

    public void setAmka(String amka) {
        this.amka = amka;
    }
}
