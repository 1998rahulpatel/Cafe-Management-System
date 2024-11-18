package cafe.management.system.controller;

import cafe.management.system.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RequestMapping("/category")
public interface CategoryController {

    @PostMapping(value = "/add")
    ResponseEntity<String> addNewCategory(@RequestBody Map<String, String> requestMap);

    @GetMapping(value = "/get")
    ResponseEntity<List<Category>> getAllCategory(@RequestParam(required = false,name = "filter") String categoryName);

    @PostMapping(value = "/update")
    ResponseEntity<String> updateCategory(@RequestBody(required = true) Map<String, String> requestMap);
}
