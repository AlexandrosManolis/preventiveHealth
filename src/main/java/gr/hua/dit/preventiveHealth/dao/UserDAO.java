package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.users.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDAO {
    //get user via user id
    public User getUserProfile(Integer user_id);

    //get user id via username
    Integer getUserId(String username);

    List<String> getAllCities();

    //save user
    public void saveUser(User user);

    public List<String> getAllSpecialties();

    public Double averageSpecialistRating(Integer userId);
}
