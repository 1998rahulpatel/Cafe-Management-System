package cafe.management.system.dao;

import cafe.management.system.model.User;
import cafe.management.system.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface UserDao extends JpaRepository<User,Integer> {
    User findByEmail(@Param("email")String email);

    @Query("select new cafe.management.system.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) from User u where u.role = 'User'")
    List<UserWrapper> findAllUser();

    @Query("select u.email from User u where u.role = 'Admin'")
    List<String> findAllAdmin();

    @Modifying
    @Query("update User u set u.status = :status where u.id = :id")
    void updateStatus(@Param("id")Integer id,@Param("status")String status);
}
