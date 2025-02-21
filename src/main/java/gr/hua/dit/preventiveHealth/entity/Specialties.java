package gr.hua.dit.preventiveHealth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class Specialties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    public Specialties() {
    }

    public Specialties(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Specialties{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
