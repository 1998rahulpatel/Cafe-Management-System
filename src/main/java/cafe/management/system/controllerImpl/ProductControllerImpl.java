package cafe.management.system.controllerImpl;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.controller.ProductController;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.model.Product;
import cafe.management.system.service.ProductService;
import cafe.management.system.util.CafeManagementSystemUtil;
import cafe.management.system.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ProductControllerImpl implements ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            return productService.addNewProduct(requestMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
            return productService.getAllProducts();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            return productService.updateProduct(requestMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProductById(Integer id) {
        try {
            return productService.deleteProductById(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProductStatus(Map<String,String> requestMap) {
        try {
            return productService.updateProductStatus(requestMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProductsByCategoryId(Integer id) {
        try {
            return productService.getAllProductsByCategoryId(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getProductById(Integer id) {
        try {
            return productService.getAllProductsById(id);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
