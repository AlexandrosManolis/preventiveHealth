package gr.hua.dit.preventiveHealth.repository.usersRepository;

import gr.hua.dit.preventiveHealth.entity.users.Specialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtiesRepository extends JpaRepository<Specialties, Integer> {

    @Query("SELECT s.recheckInterval FROM Specialties s WHERE s.specialty = :specialty")
    Integer findRecheckIntervalBySpecialty(@Param("specialty") String specialty);

}
