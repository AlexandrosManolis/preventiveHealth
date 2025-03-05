package gr.hua.dit.preventiveHealth.service;

import gr.hua.dit.preventiveHealth.dao.UserDAO;
import gr.hua.dit.preventiveHealth.entity.users.RegisterRequest;
import gr.hua.dit.preventiveHealth.entity.users.User;
import gr.hua.dit.preventiveHealth.repository.usersRepository.RegisterRequestRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterRequestService {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RegisterRequestRepository registerRequestRepository;

    @Autowired
    private EntityManager entityManager;

    //save request for role
    @Transactional
    public void saveRequest(RegisterRequest registerRequest, Integer userId) {

        User user = userDAO.getUserProfile(userId);
        registerRequest.setUser(user);

        registerRequestRepository.save(registerRequest);
    }

    //get requests that are pending
    public List<RegisterRequest> getPendingRoleRequests() {
        return registerRequestRepository.findAll();
    }

    //get users with role requests using typedQuery
    public List<User> getUsersWithRegisterRequests() {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT DISTINCT r.user FROM RegisterRequest r WHERE r.status = 'Pending' AND r.user IS NOT NULL", User.class);
        return query.getResultList();
    }

    @Transactional
    public boolean isUserPending(Integer userId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM RegisterRequest r WHERE r.status = 'PENDING' AND r.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        if (count>0) {
            return true;
        }
        return false;
    }

    public boolean isUserRejected(Integer userId) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(r) FROM RegisterRequest r WHERE r.status = 'REJECTED' AND r.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
        if (count>0) {
            return true;
        }
        return false;
    }
}
