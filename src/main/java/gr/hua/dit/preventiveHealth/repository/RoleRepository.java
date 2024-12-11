package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    //find role by name
    Optional<Role> findByRoleName(String roleName);
}
