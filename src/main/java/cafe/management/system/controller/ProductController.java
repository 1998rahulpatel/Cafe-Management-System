package cafe.management.system.controller;

import cafe.management.system.model.Product;
import cafe.management.system.wrapper.ProductWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RequestMapping(path = "/product")
public interface ProductController {

    @PostMapping(path = "/add")
    ResponseEntity<String> addNewProduct(@RequestBody(required = true) Map<String,String> requestMap);
    @GetMapping(path = "/get")
    ResponseEntity<List<ProductWrapper>> getAllProducts();
    @PostMapping(path = "/update")
    ResponseEntity<String> updateProduct(@RequestBody Map<String,String> requestMap);
    @DeleteMapping(path = "/delete/{id}")
    ResponseEntity<String> deleteProductById(@PathVariable Integer id);
    @PutMapping(path = "/update/status")
    ResponseEntity<String> updateProductStatus(@RequestBody Map<String,String> requestMap);
    @GetMapping(path = "/get/category/{id}")
    ResponseEntity<List<ProductWrapper>> getAllProductsByCategoryId(@PathVariable Integer id);
    @GetMapping(path = "/get/product/{id}")
    ResponseEntity<List<ProductWrapper>> getProductById(@PathVariable Integer id);
}
