package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDAO {
    //get user via user id
    public User getUserProfile(Integer user_id);

    //get user id via username
    Integer getUserId(String username);

    //save user
    public void saveUser(User user);

    public List<String> getAllSpecialties();
}
