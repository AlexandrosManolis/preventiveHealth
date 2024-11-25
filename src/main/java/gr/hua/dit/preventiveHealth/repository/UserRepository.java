package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByAmka(String amka);

    Boolean existsByIdentity(String identity);

    Boolean existsByAfm(String afm);
}
