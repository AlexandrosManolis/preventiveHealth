package gr.hua.dit.preventiveHealth.entity.users;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Entity
@Table(name = "registerRequest", uniqueConstraints = @UniqueConstraint(columnNames = "userId"))
public class RegisterRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    private String description;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId", nullable = false)
    @JsonBackReference("user-registerRequest")
    private User user;

    public RegisterRequest() {
    }

    @JsonProperty("requestedUserId")
    public Integer getRequestedUserId() {
        return user != null ? user.getId() : null;
    }
    @JsonProperty("fullName")
    public String getFullName() {
        return user != null ? user.getFullName() : null;
    }

    public RegisterRequest(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
