package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

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

    @ManyToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH}, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public RegisterRequest() {
    }

    public RegisterRequest(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
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
}
