package cafe.management.system.dao;

import cafe.management.system.model.Category;
import cafe.management.system.model.Product;
import cafe.management.system.wrapper.ProductWrapper;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Transactional
@Repository
public interface ProductDao extends JpaRepository<Product, Integer> {

    @Modifying
    @Query("update Product p set p.status = :status where p.id = :id")
    void updateStatus(@Param("id")Integer id, @Param("status")String status);

    @Query(value = "SELECT new cafe.management.system.wrapper.ProductWrapper(p.id,p.name) FROM Product p where p.category.id = :id and status = 'true'")
    List<ProductWrapper> getAllProductsByCategoryId(@Param("id")Integer id);

    @Query(value = "SELECT new cafe.management.system.wrapper.ProductWrapper(p.id,p.name,p.description,p.price) FROM Product p where p.id = :id")
    List<ProductWrapper> findAllById(@Param("id")Integer id);

    @Query(value = "SELECT new cafe.management.system.wrapper.ProductWrapper(p.id,p.name,p.description,p.price,p.status,p.category.id,p.category.name) FROM Product p")
    List<ProductWrapper> getAllProducts();
}
