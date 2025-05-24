package gr.hua.dit.preventiveHealth.repository;

import gr.hua.dit.preventiveHealth.entity.medicalExams.DownloadToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DownloadTokenRepository extends JpaRepository<DownloadToken, Integer> {

    @Query("SELECT d FROM DownloadToken d WHERE d.medicalExam.id = :medicalExamId")
    Optional<DownloadToken> findByMedicalExamId(@Param("medicalExamId") Integer medicalExamId);
}
