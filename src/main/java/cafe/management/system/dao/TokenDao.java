package cafe.management.system.dao;

import cafe.management.system.model.Token;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface TokenDao extends JpaRepository<Token,Integer> {

    @Query("select t from Token t where t.userId = :userId and t.isLoggedOut = false")
    List<Token> findAllTokensByUser(@Param("userId") Integer userId);

    @Query("select t from Token t where t.userId = :userId and t.isLoggedOut = true")
    List<Token> findAllInActiveTokensByUser(@Param("userId") Integer userId);

    Optional<Token> findByToken(@Param("token") String token);

}