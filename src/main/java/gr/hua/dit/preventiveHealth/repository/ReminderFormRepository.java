package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.ReminderForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderFormRepository extends JpaRepository<ReminderForm, Integer> {
    Boolean existsByPatientId(Integer userId);

    List<ReminderForm> findByPatientId(Integer userId);
}
