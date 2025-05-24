package gr.hua.dit.preventiveHealth.entity.users;

import jakarta.persistence.*;

@Entity
@Table(name = "specialties")
public class Specialties {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String specialty;

    public enum RecommendCheckUp{
        REQUIRED, OPTIONAL
    }

    private RecommendCheckUp recommendCheckUp;

    private String medicalExam;

    public enum Gender {
        MALE, FEMALE, BOTH
    }
    private Gender gender;

    private Integer minAge;
    private Integer maxAge;

    private Integer recheckInterval;

    public Specialties() {
    }

    public Specialties(String specialty, RecommendCheckUp recommendCheckUp, String medicalExam, Gender gender, Integer minAge, Integer maxAge, Integer recheckInterval) {
        if (recommendCheckUp == RecommendCheckUp.OPTIONAL) {
            throw new IllegalArgumentException("Use the other constructor for OPTIONAL check-ups.");
        }
        this.specialty = specialty;
        this.recommendCheckUp = recommendCheckUp;
        this.medicalExam = medicalExam;
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.recheckInterval = recheckInterval;
    }

    public Specialties(String specialty, RecommendCheckUp recommendCheckUp) {
        if (recommendCheckUp == RecommendCheckUp.REQUIRED) {
            throw new IllegalArgumentException("Use the other constructor for REQUIRED check-ups.");
        }
        this.specialty = specialty;
        this.recommendCheckUp = recommendCheckUp;
    }

    public Specialties(String specialty) {
        this.specialty = specialty;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String name) {
        this.specialty = name;
    }

    public String getMedicalExam() {
        return medicalExam;
    }

    public void setMedicalExam(String medicalExam) {
        this.medicalExam = medicalExam;
    }

    public RecommendCheckUp getRecommendCheckUp() {
        return recommendCheckUp;
    }

    public void setRecommendCheckUp(RecommendCheckUp recommendCheckUp) {
        this.recommendCheckUp = recommendCheckUp;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Integer getRecheckInterval() {
        return recheckInterval;
    }

    public void setRecheckInterval(Integer recheckInterval) {
        this.recheckInterval = recheckInterval;
    }

    @Override
    public String toString() {
        return "Specialties{" +
                "id=" + id +
                ", name='" + specialty + '\'' +
                '}';
    }
}
