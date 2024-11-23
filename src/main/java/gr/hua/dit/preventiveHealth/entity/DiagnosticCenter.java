package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "diagnosticCenters",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class DiagnosticCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @MapsId
    private User user;

    @NotBlank
    private String address;

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

    @OneToMany(mappedBy = "diagnosticCenter", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    public DiagnosticCenter() {
    }

    public DiagnosticCenter(String address, String state, String doy, String afm, List<String> specialties, List<Schedule> schedules, User user) {
        this.address = address;
        this.state = state;
        this.doy = doy;
        this.afm = afm;
        this.specialties = specialties;
        this.schedules = schedules;
        this.user = user;
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

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
