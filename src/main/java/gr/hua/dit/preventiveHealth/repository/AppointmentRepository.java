package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

}
