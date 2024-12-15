package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    Optional<Schedule> findByDoctor_Id(Integer userId);
}
