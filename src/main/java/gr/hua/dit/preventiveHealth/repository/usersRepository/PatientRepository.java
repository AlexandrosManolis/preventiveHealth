package gr.hua.dit.preventiveHealth.repository.usersRepository;

import gr.hua.dit.preventiveHealth.entity.users.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    @Query("SELECT p.folderName FROM Patient p WHERE p.id = :id")
    String findFolderNameById(@Param("id") Integer id);
}
