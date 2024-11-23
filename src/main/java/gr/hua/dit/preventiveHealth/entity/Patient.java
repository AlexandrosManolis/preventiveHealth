package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Table(name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "amka"),
                @UniqueConstraint(columnNames = "identity")
        })
public class Patient{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @MapsId
    private User user;

    public enum Gender {
        MALE,
        FEMALE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthday;

    @NotBlank
    @Size(min = 9, max = 9, message = "Number should contain exactly 9 digits.")
    @Pattern(regexp = "\\d+", message = "Number should contain only digits.")
    private String amka;

    @NotBlank
    @Size(min=8, max=8)
    @Pattern(regexp = "[A-Z]{2}\\d{6}", flags = Pattern.Flag.MULTILINE)
    private String identity;

    private LocalDate parseBirthday(String birthdayStr) {
        if (birthdayStr == null || birthdayStr.isEmpty()) {
            return null; // Handle empty birthday
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(birthdayStr, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected dd/MM/yyyy", e);
        }
    }

    public Patient() {
    }

    public Patient(Gender gender, String birthdayStr, String amka, String identity) {
        this.gender = gender;
        this.birthday = parseBirthday(birthdayStr);
        this.amka = amka;
        this.identity = identity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
