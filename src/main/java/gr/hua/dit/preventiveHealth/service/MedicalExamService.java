package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.entity.MedicalExam;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.MedicalExamRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalExamService {

    @Autowired
    MedicalExamRepository medicalExamRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    MinioService minioService;

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UserService userService;

    public String uploadExam(Integer appointmentId, MultipartFile file) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment ID not found: " + appointmentId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Integer doctorId = userDAO.getUserId(username);
        String userRole = userService.getUserRole();

        if (userRole.equals("ROLE_DOCTOR")) {
            if (!appointment.getDoctor().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized: Only the assigned doctor can upload exams");
            }
        } else if (userRole.equals("ROLE_DIAGNOSTIC")) {
            if (!appointment.getDiagnosticCenter().getId().equals(doctorId)) {
                throw new RuntimeException("Unauthorized: Only the assigned diagnostic center can upload exams");
            }
        }

        String patientFolder = appointment.getPatient().getFullName().replaceAll("\\s+", "_") + "_" + appointment.getPatient().getId()
                + "/" + "appointments/" + appointmentId + "/";

        // Define full file path inside patient folder
        String filePath = patientFolder + file.getOriginalFilename();

        minioService.uploadFile(filePath, file);

        // Save metadata to database
        MedicalExam exam = new MedicalExam();
        exam.setFileName(file.getOriginalFilename());
        exam.setFilePath(filePath);
        exam.setPatient(appointment.getPatient());

        medicalExamRepository.save(exam);
        appointment.setMedicalExam(exam);
        appointmentRepository.save(appointment);

        return filePath;
    }

    public List<Map<String, Object>> getFileRecords(Integer patientId) {
        return medicalExamRepository.findByPatientId(patientId)
                .stream()
                .map(exam -> {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("fileName", exam.getFileName());
                    fileData.put("url", minioService.getPresignedUrl(exam.getFilePath()));

                    Appointment appointment = exam.getAppointment();
                    if (appointment != null) {
                        fileData.put("date", appointment.getDate());
                        fileData.put("specialty", appointment.getSpecialty());
                        fileData.put("diagnosis", appointment.getDiagnosisDescription());
                    }

                    return fileData;
                })
                .collect(Collectors.toList());
    }
}

