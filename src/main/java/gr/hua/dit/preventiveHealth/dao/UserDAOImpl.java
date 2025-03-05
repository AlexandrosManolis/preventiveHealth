package gr.hua.dit.preventiveHealth.dao;

import gr.hua.dit.preventiveHealth.entity.users.RegisterRequest;
import gr.hua.dit.preventiveHealth.entity.users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO{


    @PersistenceContext
    private EntityManager entityManager;

    //get user profile
    @Override
    public User getUserProfile(Integer user_id) {
        return entityManager.find(User.class, user_id);
    }

    //get user id via username using Query
    @Override
    public Integer getUserId(String username) {
        try {
            Integer userId = entityManager.createQuery(
                            "SELECT u.id FROM User u WHERE u.username = :username", Integer.class)
                    .setParameter("username", username)
                    .getSingleResult();

            return userId;
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public Double averageSpecialistRating(Integer userId) {
        try {
            Double allSpecialistsRatedByUser = entityManager.createQuery(
                            "SELECT AVG(r.rating) FROM RatingSpecialist r WHERE r.doctor.id = :userId OR r.diagnosticCenter.id = :userId", Double.class)
                    .setParameter("userId", userId)
                    .getSingleResult();

            return allSpecialistsRatedByUser;
        } catch (NoResultException | NonUniqueResultException ex) {
            return null;
        }
    }

    @Override
    public List<String> getAllSpecialties() {
        try {
            List<String> specialties = entityManager.createQuery(
                            "SELECT DISTINCT d.specialty " +
                                    "FROM Doctor d JOIN d.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status " +
                                    "UNION " +
                                    "SELECT DISTINCT c.specialties " +
                                    "FROM DiagnosticCenter c JOIN c.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status", String.class)
                    .setParameter("status", RegisterRequest.Status.ACCEPTED)
                    .getResultList();
            return specialties;
        } catch (NoResultException ex) {
            return Collections.emptyList(); // Return an empty list instead of null
        }
    }

    @Override
    public List<String> getAllCities() {
        try {
            List<String> cities = entityManager.createQuery(
                            "SELECT DISTINCT d.city " +
                                    "FROM Doctor d JOIN d.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status " +
                                    "UNION " +
                                    "SELECT DISTINCT c.city " +
                                    "FROM DiagnosticCenter c JOIN c.user u JOIN RegisterRequest rr ON rr.user = u " +
                                    "WHERE rr.status = :status", String.class)
                    .setParameter("status", RegisterRequest.Status.ACCEPTED)
                    .getResultList();
            return cities;
        } catch (NoResultException ex) {
            return Collections.emptyList(); // Return an empty list instead of null
        }
    }


    //save user
    @Transactional
    public void saveUser(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
    }

}
