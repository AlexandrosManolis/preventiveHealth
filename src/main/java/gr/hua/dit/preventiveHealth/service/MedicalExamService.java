package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExam;
import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExamSharing;
import gr.hua.dit.preventiveHealth.entity.users.Patient;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.MedicalExamRepository;

import gr.hua.dit.preventiveHealth.repository.MedicalExamSharingRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MedicalExamService {
    private static final Logger logger = LoggerFactory.getLogger(MedicalExamService.class);

    @Autowired
    private MedicalExamRepository medicalExamRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private MedicalExamSharingRepository medicalExamSharingRepository;

    /**
     * Upload a medical exam file for an appointment
     */
    public void uploadExam(Integer appointmentId, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                logger.warn("No file provided for upload for appointment {}", appointmentId);
                throw new IllegalArgumentException("No file provided for upload");
            }

            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment ID not found: " + appointmentId));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Integer uploaderId = userDAO.getUserId(username);
            String userRole = userService.getUserRole();

            logger.info("Uploading exam for appointment: {}, uploaded by user: {} with role: {}",
                    appointmentId, uploaderId, userRole);

            // Authorization check
            if ("ROLE_DOCTOR".equals(userRole)) {
                if (appointment.getDoctor() == null || !appointment.getDoctor().getUser().getId().equals(uploaderId)) {
                    logger.warn("Unauthorized doctor access: {} attempted to upload for appointment {}",
                            uploaderId, appointmentId);
                    throw new SecurityException("Unauthorized: Only the assigned doctor can upload exams.");
                }
            } else if ("ROLE_DIAGNOSTIC".equals(userRole)) {
                if (appointment.getDiagnosticCenter() == null ||
                        !appointment.getDiagnosticCenter().getUser().getId().equals(uploaderId)) {
                    logger.warn("Unauthorized diagnostic center access: {} attempted to upload for appointment {}",
                            uploaderId, appointmentId);
                    throw new SecurityException("Unauthorized: Only the assigned diagnostic center can upload exams.");
                }
            } else {
                logger.warn("Unauthorized role: {} attempted to upload for appointment {}",
                        userRole, appointmentId);
                throw new SecurityException("Unauthorized role.");
            }

            MedicalExam exam = new MedicalExam();

            String patientFolderName = patientRepository.findFolderNameById(appointment.getPatient().getId());
            if (patientFolderName == null || patientFolderName.isEmpty()) {
                patientFolderName = UUID.randomUUID() + "_" + appointment.getPatient().getId();

                Patient patient = patientRepository.findById(appointment.getPatient().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + appointment.getPatient().getId()));
                patient.setFolderName(patientFolderName);
                patientRepository.save(patient);
            }
            String patientFolder = String.format("%s/%s/", patientFolderName, appointment.getSpecialty());

            // Generate unique file name
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String safeFileName = UUID.randomUUID() + (fileExtension.isEmpty() ? "" : "." + fileExtension);
            String filePath = patientFolder + safeFileName;

            // Upload file to MinIO
            minioService.uploadFile(filePath, file, appointment.getPatient().getId());
            logger.info("File uploaded successfully to path: {}", filePath);

            // Save metadata to database
            exam.setFileName(safeFileName);
            exam.setFilePath(filePath);
            exam.setPatient(appointment.getPatient());
            exam.setAppointment(appointment);

            medicalExamRepository.save(exam);

            appointment.setMedicalExam(exam);
            appointmentRepository.save(appointment);
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Security violation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error uploading exam for appointment {}: {}", appointmentId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload medical exam: " + e.getMessage(), e);
        }
    }

    /**
     * Get file records for a patient
     */
    public List<Map<String, Object>> getFileRecords(Integer userId) {
        try {
            logger.info("Retrieving file records for patient: {}", userId);
            String userRole = userService.getUserRole();

            List<MedicalExam> allMedicalExams = new ArrayList<>();

            switch (userRole) {
                case "ROLE_PATIENT" -> {
                    return medicalExamRepository.findByPatientId(userId)
                            .stream()
                            .map(exam -> {
                                Map<String, Object> fileData = new HashMap<>();
                                fileData.put("id", exam.getId());
                                fileData.put("fileName", exam.getFileName());

                                Appointment appointment = exam.getAppointment();
                                if (appointment != null) {
                                    fileData.put("appointmentId", appointment.getId());
                                    fileData.put("date", appointment.getDate());
                                    fileData.put("specialty", appointment.getSpecialty());
                                    fileData.put("diagnosis", appointment.getDiagnosisDescription());
                                }
                                return fileData;
                            })
                            .collect(Collectors.toList());
                }
                case "ROLE_DOCTOR" -> {
                    List<MedicalExamSharing> allMedicalExamSharing = medicalExamSharingRepository.findAllByDoctorId(userId);
                    for (MedicalExamSharing sharing : allMedicalExamSharing) {
                        if(sharing.getExpirationTime().isBefore(LocalDateTime.now())){
                            medicalExamSharingRepository.delete(sharing);
                        }else{
                            allMedicalExams.add(medicalExamRepository.findById(sharing.getMedicalExam().getId()).orElseThrow(() -> new RuntimeException("No medical exam with this id found.")));
                        }
                    }
                }
                case "ROLE_DIAGNOSTIC" -> {
                    List<MedicalExamSharing> allMedicalExamSharing = medicalExamSharingRepository.findAllByDiagnosticCenterId(userId);
                    for (MedicalExamSharing sharing : allMedicalExamSharing) {
                        if(sharing.getExpirationTime().isBefore(LocalDateTime.now())){
                            medicalExamSharingRepository.delete(sharing);
                        }else{
                            allMedicalExams.add(medicalExamRepository.findById(sharing.getMedicalExam().getId()).orElseThrow(() -> new RuntimeException("No medical exam with this id found.")));
                        }
                    }
                }
            }

            // Common return for doctor/diagnostic role
            return allMedicalExams.stream()
                    .map(exam -> {
                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("id", exam.getId());
                        fileData.put("fileName", exam.getFileName());

                        Appointment appointment = exam.getAppointment();
                        if (appointment != null) {
                            fileData.put("appointmentId", appointment.getId());
                            fileData.put("date", appointment.getDate());
                            fileData.put("specialty", appointment.getSpecialty());
                            fileData.put("diagnosis", appointment.getDiagnosisDescription());
                            fileData.put("patientFullName", appointment.getPatient().getFullName());
                            fileData.put("patientId", appointment.getPatient().getId());
                        }
                        return fileData;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error retrieving file records for patient {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve file records: " + e.getMessage(), e);
        }
    }


    // Helper method to get file extension
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
