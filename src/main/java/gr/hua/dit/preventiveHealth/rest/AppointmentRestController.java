package gr.hua.dit.preventiveHealth.rest;

import gr.hua.dit.preventiveHealth.dao.AppointmentDAO;
import gr.hua.dit.preventiveHealth.entity.*;
import gr.hua.dit.preventiveHealth.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("api/appointment")
public class AppointmentRestController {

    @Autowired
    private AppointmentDAO appointmentDAO;

    @GetMapping("timeslots/{specialistId}")
    public ResponseEntity<?> generateTimeSlots(@PathVariable Integer specialistId, @RequestParam("date") String date) {
        LocalDate requestedDate = LocalDate.parse(date);

        List<String> slots = new ArrayList<>();
        LocalTime now = LocalTime.now().plusHours(3);
        Integer nowMinutes = now.getHour() * 60 + now.getMinute();

        List<Object[]> availableOpeningHours =appointmentDAO.availableOpeningHours(specialistId, requestedDate);

        if(availableOpeningHours.isEmpty()){
            return ResponseEntity.badRequest().body(new MessageResponse("Doctor is closed the date you requested."));
        }else{
            Integer startTime = appointmentDAO.convertToMinutes((String) availableOpeningHours.get(0)[0]);
            Integer endTime = appointmentDAO.convertToMinutes((String) availableOpeningHours.get(0)[1]) -20;
            boolean isToday = LocalDate.now().equals(date);

            List<Appointment> existAppointment = appointmentDAO.specialistPendingAppointment(specialistId, requestedDate);

            List<Integer> notAvailableTime = existAppointment.stream().map(Appointment::getTime)
                    .map(time -> appointmentDAO.convertToMinutes(time)).toList();

            for (int time = startTime; time < endTime; time += 30) {
                final int currentTime = time;
                boolean isNearUnavailableTime = notAvailableTime.stream()
                        .anyMatch(unavailableTime -> Math.abs(unavailableTime - currentTime) <= 10);

                if ((!isToday || time > nowMinutes) && (!notAvailableTime.contains(time)) && (!isNearUnavailableTime)) {
                    String hours = String.format("%02d", time / 60);
                    String minutes = String.format("%02d", time % 60);
                    slots.add(hours + ":" + minutes);
                }
            }
            return new ResponseEntity<>(slots, HttpStatus.OK);
        }
    }
}