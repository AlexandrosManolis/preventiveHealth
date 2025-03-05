package gr.hua.dit.preventiveHealth.repository.usersRepository;

import gr.hua.dit.preventiveHealth.entity.users.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    List<Doctor> findBySpecialty(String specialty);

    List<Doctor> findByCity(String city);
}
