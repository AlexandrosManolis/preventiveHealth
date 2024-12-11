package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.RegisterRequest;
import gr.hua.dit.preventiveHealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterRequestRepository extends JpaRepository<RegisterRequest, Integer> {


    Boolean existsByUser(User user);

    Boolean existsByUserId(Integer userId);
}
