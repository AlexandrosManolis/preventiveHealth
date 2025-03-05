package gr.hua.dit.preventiveHealth.repository.usersRepository;

import gr.hua.dit.preventiveHealth.entity.users.RegisterRequest;
import gr.hua.dit.preventiveHealth.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisterRequestRepository extends JpaRepository<RegisterRequest, Integer> {


    Boolean existsByUser(User user);

    Boolean existsByUserId(Integer userId);

    Optional<RegisterRequest> findByUserId(Integer userId);

    List<RegisterRequest> findByStatus(RegisterRequest.Status status);
}
