package cafe.management.system.service;

import cafe.management.system.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ResponseEntity<String> addNewProduct(Map<String, String> requestMap);

    ResponseEntity<List<ProductWrapper>> getAllProducts();

    ResponseEntity<String> updateProduct(Map<String, String> requestMap);

    ResponseEntity<String> deleteProductById(Integer id);

    ResponseEntity<String> updateProductStatus(Map<String,String> requestMap);

    ResponseEntity<List<ProductWrapper>> getAllProductsByCategoryId(Integer id);

    ResponseEntity<List<ProductWrapper>> getAllProductsById(Integer id);
}
