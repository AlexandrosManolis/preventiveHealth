package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.users.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDAO {
    //get user via user id
    User getUserProfile(Integer user_id);

    //get user id via username
    Integer getUserId(String username);

    List<String> getAllCities();

    //save user
    void saveUser(User user);

    List<String> getAllSpecialties();

    Double averageSpecialistRating(Integer userId);

    User createDoctorUser(String username);
}
