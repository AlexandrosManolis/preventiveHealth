package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.PatientRepository;
import io.minio.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.stream.StreamSupport;

@Service
public class MinioService {
    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Autowired
    private PatientRepository patientRepository;

    public byte[] getFileContent(String filePath) {
        try {
            // Get object from MinIO
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );

            // Read the file content into a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[16384]; // 16KB buffer
            int bytesRead;

            while ((bytesRead = response.read(buffer, 0, buffer.length)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            response.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error getting file content for {}: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("Error getting file content: " + e.getMessage(), e);
        }
    }

    public void uploadFile(String filePath, MultipartFile file, Integer patientId) {
        try {
            verifyFileAccessAuthorization(patientId);
            logger.info("Uploading file to path: {} for patient: {}", filePath, patientId);

            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                logger.info("Bucket {} does not exist. Creating...", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build()
                );
                logger.info("File uploaded successfully to path: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("Error uploading file {}: {}", filePath, e.getMessage(), e);
            throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * List files for a specific appointment
     */
    public String listPatientFile(Integer patientId, String specialty) {
        try {
            verifyFileAccessAuthorization(patientId);
            String prefix = String.format("%s/%s/", patientRepository.findFolderNameById(patientId), specialty);
            logger.info("Searching for file with prefix: {}", prefix);

            return StreamSupport.stream(
                            minioClient.listObjects(ListObjectsArgs.builder()
                                    .bucket(bucketName).prefix(prefix).recursive(true).build()
                            ).spliterator(), false)
                    .map(result -> {
                        try {
                            return result.get().objectName();
                        } catch (Exception e) {
                            logger.error("Failed to retrieve object name: {}", e.getMessage(), e);
                            throw new RuntimeException("Failed to retrieve object name from MinIO response", e);
                        }
                    }).findFirst().orElse(null);
        } catch (Exception e) {
            logger.error("Error listing file for patient {}: {}",
                    patientId, e.getMessage(), e);
            throw new RuntimeException("Failed to list patient files from MinIO: " + e.getMessage(), e);
        }
    }

    // ------------ Authorization & Consent ------------

    private void verifyFileAccessAuthorization(Integer patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer userId = userDAO.getUserId(username);
        String userRole = userService.getUserRole();

        boolean authorized = false;

        if ("ROLE_PATIENT".equals(userRole) && userId.equals(patientId)) {
            authorized = true;
        } else if ("ROLE_DOCTOR".equals(userRole)) {
            authorized = appointmentRepository.existsByPatientIdAndDoctorId(patientId, userId);
        } else if ("ROLE_DIAGNOSTIC".equals(userRole)) {
            authorized = appointmentRepository.existsByPatientIdAndDiagnosticId(patientId, userId);
        } else if ("ROLE_ADMIN".equals(userRole)) {
            authorized = true;
        }

        if (!authorized) {
            logger.warn("Unauthorized access attempt for patient {} by user {} with role {}",
                    patientId, userId, userRole);
            throw new SecurityException("Unauthorized access attempt.");
        }
    }
}
