package cafe.management.system.dao;

import cafe.management.system.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDao extends JpaRepository<Bill, Integer> {

    @Query("SELECT b FROM Bill b order by b.id desc")
    List<Bill> findAll();

    @Query("SELECT b FROM Bill b WHERE b.createdBy = :email order by b.id desc")
    List<Bill> findAllByEmail(@Param("email") String email);
}