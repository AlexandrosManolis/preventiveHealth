package gr.hua.dit.preventiveHealth.entity.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import gr.hua.dit.preventiveHealth.entity.ReminderForm;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Entity
@Table(name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "amka")
        })
public class Patient{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference("user-patient")
    private User user;

    public enum Gender {
        MALE,
        FEMALE
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull
    @Past(message = "Birthday must be in the past") // Example of an additional validation for a past date
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @NotBlank
    @Size(min = 11, max = 11, message = "Number should contain exactly 11 digits.")
    @Pattern(regexp = "\\d+", message = "Number should contain only digits.")
    private String amka;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ReminderForm> reminderForm;

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

    private String folderName;

    public Patient() {
    }

    public List<ReminderForm> getReminderForm() {
        return reminderForm;
    }

    public void setReminderForm(List<ReminderForm> reminderForm) {
        this.reminderForm = reminderForm;
    }

    public Patient(User user, Gender gender, String birthdayStr, String amka) {
        this.user = user;
        this.gender = gender;
        this.birthday = parseBirthday(birthdayStr);
        this.amka = amka;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @JsonProperty("fullName")
    public String getFullName() {
        return user != null ? user.getFullName() : null;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", gender=" + gender +
                ", birthday=" + birthday +
                ", amka='" + amka + '\'' +
                '}';
    }
}
