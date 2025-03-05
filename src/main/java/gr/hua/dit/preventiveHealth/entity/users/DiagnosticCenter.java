package gr.hua.dit.preventiveHealth.entity.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "diagnosticCenters",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "afm")
        })
public class DiagnosticCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonManagedReference("diagnostic-schedule")
    @OneToMany(mappedBy = "diagnosticCenter", cascade = CascadeType.ALL)
    private List<OpeningHours> openingHours;

    @OneToOne
    @MapsId
    @JsonBackReference("user-diagnostic")
    @JoinColumn(name = "id")
    private User user;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String doy;

    @NotBlank
    @Size(min = 9, max = 9, message = "Number should contain exactly 9 digits.")
    @Pattern(regexp = "\\d+", message = "Number should contain only digits.")
    private String afm;


    @NotEmpty
    @ElementCollection
    @CollectionTable(
            name = "diagnostic_center_specialties",
            joinColumns = @JoinColumn(name = "diagnostic_center_id")
    )
    @Column(name = "specialty")
    private List<String> specialties;

    public DiagnosticCenter() {
    }

    public DiagnosticCenter(String address,String city, String state,  String doy, String afm, List<String> specialties, List<OpeningHours> openingHours, User user) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.doy = doy;
        this.afm = afm;
        this.specialties = specialties;
        this.openingHours = openingHours;
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDoy() {
        return doy;
    }

    public void setDoy(String doy) {
        this.doy = doy;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
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
        return "DiagnosticCenter{" +
                "id=" + id +
                ", openingHours=" + openingHours +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", doy='" + doy + '\'' +
                ", afm='" + afm + '\'' +
                ", specialties=" + specialties +
                '}';
    }
}
