package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.Appointment;
import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExam;
import gr.hua.dit.preventiveHealth.entity.medicalExams.DownloadToken;
import gr.hua.dit.preventiveHealth.entity.medicalExams.MedicalExamSharing;
import gr.hua.dit.preventiveHealth.entity.users.User;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import gr.hua.dit.preventiveHealth.repository.AppointmentRepository;
import gr.hua.dit.preventiveHealth.repository.DownloadTokenRepository;
import gr.hua.dit.preventiveHealth.repository.MedicalExamRepository;
import gr.hua.dit.preventiveHealth.repository.MedicalExamSharingRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.DiagnosticRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.DoctorRepository;
import gr.hua.dit.preventiveHealth.repository.usersRepository.UserRepository;
import gr.hua.dit.preventiveHealth.service.GmailService;
import gr.hua.dit.preventiveHealth.service.MedicalExamService;
import gr.hua.dit.preventiveHealth.service.MinioService;
import gr.hua.dit.preventiveHealth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("api/medicalRecord")
public class MedicalRecordRestController {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MedicalExamService medicalExamService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MinioService minioService;

    @Autowired
    private MedicalExamRepository medicalExamRepository;
    @Autowired
    private GmailService gmailService;
    @Autowired
    private DownloadTokenRepository downloadTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DiagnosticRepository diagnosticRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private MedicalExamSharingRepository medicalExamSharingRepository;
    @Autowired
    private UserService userService;

    @GetMapping("{userId}/examFiles")
    public ResponseEntity<?> getAllExamFile(@PathVariable Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Integer user_id = userDAO.getUserId(authentication.getName());
        if (userId.equals(user_id)){
                List<Map<String,Object>> medicalExams= medicalExamService.getFileRecords(userId);
                return ResponseEntity.ok(medicalExams);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to access this source.");
    }

    @GetMapping("{userId}/requestDownload/{medicalExamId}")
    public ResponseEntity<?> emailVerificationForDownload(@PathVariable Integer userId, @PathVariable Integer medicalExamId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer user_id = userDAO.getUserId(authentication.getName());
        DownloadToken downloadToken = downloadTokenRepository.findByMedicalExamId(medicalExamId).orElse(new DownloadToken());
        MedicalExam medicalExam = medicalExamRepository.findById(medicalExamId).orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + medicalExamId));

        if (userId.equals(user_id) && medicalExam.getPatient().getId().equals(user_id)){

            downloadToken.setVerificationToken(UUID.randomUUID().toString());
            downloadToken.setMedicalExam(medicalExam);
            downloadToken.setStatus(DownloadToken.Status.PENDING);
            downloadTokenRepository.save(downloadToken);

            String bodyText = "To verify your download action, please use the following token:<br><br>" +
                    "<div style='display: inline-block; padding: 15px; background-color: #f5f5f5; border: 1px solid #ddd; border-radius: 5px; font-family: monospace; font-size: 18px;'>" +
                    downloadToken.getVerificationToken() +
                    "</div><br><br>" +
                    "Please enter this token in the verification field on our website to complete your download.";

            gmailService.sendEmail(medicalExam.getPatient().getUser().getEmail(), "Email verification", bodyText);

            return ResponseEntity.ok(downloadToken.getStatus());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to access this source");

    }

    @PostMapping("{userId}/verifyDownload/{medicalExamId}")
    public ResponseEntity<?> verifyDownload(@PathVariable Integer userId, @PathVariable Integer medicalExamId, @RequestBody String rawToken){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer user_id = userDAO.getUserId(authentication.getName());
        DownloadToken downloadToken = downloadTokenRepository.findByMedicalExamId(medicalExamId).orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + medicalExamId));

        if (userId.equals(user_id) && downloadToken.getMedicalExam().getPatient().getUser().getId().equals(user_id)){
            String verificationToken = rawToken.trim();
            verificationToken = verificationToken.replace("\"", "");

            if (downloadToken.getStatus() == DownloadToken.Status.PENDING && downloadToken.getVerificationToken().equals(verificationToken)){
                downloadToken.setStatus(DownloadToken.Status.COMPLETED);
                downloadTokenRepository.save(downloadToken);

                gmailService.sendEmail(downloadToken.getMedicalExam().getPatient().getUser().getEmail(), "Email verification completed.", "Email verified successfully.");
                System.out.println(getExamFileForViewer(userId, downloadToken.getMedicalExam().getAppointment().getId()));
                return getExamFileForViewer(userId, downloadToken.getMedicalExam().getAppointment().getId());
            }else {
                return ResponseEntity.badRequest().body("Wrong/Completed token.");
            }
        }

        return ResponseEntity.badRequest().body("Something went wrong. Try again.");
    }

    @GetMapping("{userId}/appointments/{appointmentId}/details/examFile")
    public ResponseEntity<?> getExamFileForViewer(@PathVariable Integer userId, @PathVariable Integer appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

            // Security Check: Verify appointment status
            if (appointment.getAppointmentStatus() != Appointment.AppointmentStatus.COMPLETED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Medical exams are available only after appointment completion."));
            }

            // Get the file path from MinIO
            String filePath = minioService.listPatientFile(appointment.getPatient().getId(), appointment.getSpecialty());

            if (filePath == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Exam file not found for this appointment."));
            }

            // Stream the file content as binary data rather than returning a URL
            byte[] pdfContent = minioService.getFileContent(filePath.trim());

            // Return the PDF content with proper headers for secure viewing
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("medical-record.pdf")
                    .build());
            // Disable caching to prevent saving
            headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
            headers.setPragma("no-cache");
            headers.add("X-Content-Type-Options", "nosniff");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving exam file: " + e.getMessage()));
        }
    }

    @GetMapping("{userId}/providers")
    public ResponseEntity<?> getDoctorsForShare(@PathVariable Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer user_id = userDAO.getUserId(authentication.getName());

        if (userId.equals(user_id)) {
            try {
                List<Appointment> appointments = appointmentRepository.findByPatientId(userId);

                Set<User> allUsers = new HashSet<>();

                for (Appointment appointment : appointments) {
                    if (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.COMPLETED || (appointment.getAppointmentStatus() == Appointment.AppointmentStatus.PENDING
                            && appointment.getAppointmentRequestStatus() == Appointment.AppointmentRequestStatus.APPROVED)) {
                        if (appointment.getDoctor() != null) {
                            User user = appointment.getDoctor().getUser();
                            user.setPassword("");
                            allUsers.add(user);
                        } else {
                            User user = appointment.getDiagnosticCenter().getUser();
                            user.setPassword("");
                            allUsers.add(user);
                        }
                    }
                }

                return ResponseEntity.ok(new ArrayList<>(allUsers));
            } catch (Exception e) {
                throw new RuntimeException("Failed to retrieve file records: " + e.getMessage(), e);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorised access resource.");
    }

    @PostMapping("{userId}/shareFile/{specialistId}")
    public ResponseEntity<?> shareToDoctor(@PathVariable Integer userId, @PathVariable Integer specialistId, @RequestBody Map<String, List<Map<String, Object>>> request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer user_id = userDAO.getUserId(authentication.getName());

        User user = userRepository.findById(specialistId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + specialistId));
        Boolean exists = false;
        MedicalExamSharing medicalExamSharing = new MedicalExamSharing();
        if (userId.equals(user_id)){
            if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_DIAGNOSTIC"))){
                exists = appointmentRepository.existsByPatientIdAndDiagnosticId(userId, specialistId);
            }else if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_DOCTOR"))){
                exists = appointmentRepository.existsByPatientIdAndDoctorId(userId, specialistId);
            }
            if (exists){
                List<Map<String, Object>> exams = request.get("exams");
                for(Map<String, Object> exam : exams) {
                    Integer id = (Integer) exam.get("medicalExamId");
                    String name = (String) exam.get("medicalExamName");

                    MedicalExam medicalExam = medicalExamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Exam not found with ID: " + id));
                    medicalExamSharing = new MedicalExamSharing();
                    if (medicalExam.getFileName().equals(name)){
                        medicalExamSharing.setMedicalExam(medicalExam);
                    }else {
                        return ResponseEntity.badRequest().body("No match exam id with name");
                    }

                    if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_DIAGNOSTIC"))) {
                        medicalExamSharing.setDiagnosticCenter(diagnosticRepository.findById(specialistId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + specialistId)));
                    } else if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_DOCTOR"))) {
                        medicalExamSharing.setDoctor(doctorRepository.findById(specialistId).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + specialistId)));
                    }
                    medicalExamSharing.setExpirationTime(LocalDateTime.now().plusMinutes(5));

                    medicalExamSharingRepository.save(medicalExamSharing);
                }
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You have no access to share files with the selected specialist.");
            }
        }
        return ResponseEntity.ok(medicalExamSharing);
    }

}