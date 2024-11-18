package cafe.management.system.service;

import cafe.management.system.model.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    ResponseEntity<String> addNewCategory(Map<String, String> requestMap);

    ResponseEntity<List<Category>> getAllCategory(String categoryName);

    ResponseEntity<String> updateCategory(Map<String, String> requestMap);
}
