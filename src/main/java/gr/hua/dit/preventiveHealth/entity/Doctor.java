package gr.hua.dit.preventiveHealth.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "afm")
        })
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonManagedReference("doctor-schedule")
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpeningHours> openingHours;

    @OneToOne
    @MapsId
    @JsonBackReference("user-doctor")
    @JoinColumn(name = "id")
    private User user;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String specialty;

    @NotBlank
    private String doy;

    @NotBlank
    @Size(min = 9, max = 9, message = "Number should contain exactly 9 digits.")
    @Pattern(regexp = "\\d+", message = "Number should contain only digits.")
    private String afm;

    public Doctor() {
    }

    public Doctor(User user, String address, String city, String specialty, String state, String doy, String afm, List<OpeningHours> openingHours) {
        this.user = user;
        this.address = address;
        this.city = city;
        this.specialty = specialty;
        this.state = state;
        this.doy = doy;
        this.afm = afm;
        this.openingHours = openingHours;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getDoy() {
        return doy;
    }

    public void setDoy(String doy) {
        this.doy = doy;
    }

    public String getAfm() {
        return afm;
    }

    public void setAfm(String afm) {
        this.afm = afm;
    }

    public List<OpeningHours> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(List<OpeningHours> openingHours) {
        this.openingHours = openingHours;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonProperty("fullName") // Explicitly include in JSON
    public String getFullName() {
        return user != null ? user.getFullName() : null;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", openingHours=" + openingHours +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", specialty='" + specialty + '\'' +
                ", doy='" + doy + '\'' +
                ", afm='" + afm + '\'' +
                '}';
    }
}
