package gr.hua.dit.preventiveHealth.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MinioService {

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public InputStream downloadFile(String filePath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(filePath).build());
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Security error while downloading the file: " + e.getMessage(), e);
        } catch (MinioException e) {
            throw new RuntimeException("Error downloading the file from MinIO: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error occurred while downloading the file: " + e.getMessage(), e);
        }
    }


    public void uploadFile(String filePath, MultipartFile file) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String contentType = file.getContentType() != null ? file.getContentType() :
                    Files.probeContentType(file.getResource().getFile().toPath());

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(contentType)
                                .build());
            }

            System.out.println("File uploaded successfully: " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO: " + filePath, e);
        }
    }

    public List<String> listPatientFiles(String fullName, Integer patientId) {
        String prefix = String.format("patients/%s_%d/appointments/",
                fullName.replaceAll("\\s+", "_"), patientId);

        try {
            return StreamSupport.stream(minioClient.listObjects(
                                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build())
                            .spliterator(), false)
                    .map(result -> {
                        try {
                            return result.get().objectName();
                        } catch (Exception e) {
                            throw new RuntimeException("Error fetching object name", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error listing files", e);
        }
    }

    public String listPatientFile(String fullName, Integer patientId, Integer appointmentId) {
        String prefix = String.format("patients/%s_%d/appointments/%d/",
                fullName.replaceAll("\\s+", "_"), patientId, appointmentId);
        System.out.println("Generated prefix: " + prefix);

        try {
            return StreamSupport.stream(minioClient.listObjects(
                                    ListObjectsArgs.builder().bucket(bucketName).prefix(prefix).recursive(true).build())
                            .spliterator(), false)
                    .map(result -> {
                        try {
                            return result.get().objectName();
                        } catch (Exception e) {
                            throw new RuntimeException("Error fetching object name", e);
                        }
                    })
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            throw new RuntimeException("Error listing file for patient and appointment", e);
        }
    }
}
