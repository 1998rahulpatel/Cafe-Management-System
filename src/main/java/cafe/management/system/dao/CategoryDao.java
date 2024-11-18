package cafe.management.system.dao;

import cafe.management.system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryDao extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.id in (SELECT p.category.id FROM Product p WHERE p.status = 'true')")
    List<Category> getAllCategory();
    List<Category> findAllByName(String categoryName);
}
