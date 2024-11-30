package cafe.management.system.serviceImpl;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.dao.ProductDao;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.model.Category;
import cafe.management.system.model.Product;
import cafe.management.system.service.ProductService;
import cafe.management.system.util.CafeManagementSystemUtil;
import cafe.management.system.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        log.info("Adding new product");
        try {
            if(jwtFilter.isAdmin()) {
                log.info("Admin user is adding a product");
                if(validateProductMap(requestMap,false)) {
                    productDao.save(getProductFromMap(requestMap, false));
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_ADDED_SUCCESSFULLY, HttpStatus.OK);
                }
                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            else {
                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_NOT_ALLOWED_TO_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdmin) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));
        Product product = new Product();
        if (isAdmin){
           product.setId(Integer.parseInt(requestMap.get("id")));
        }
        else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        return product;
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
            return new ResponseEntity<>(productDao.getAllProducts(),HttpStatus.OK);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<ProductWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap,true)) {
                    Optional<Product> optionalProduct = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!optionalProduct.isEmpty()) {
                        Product product = getProductFromMap(requestMap, true);
                        product.setStatus(optionalProduct.get().getStatus());
                        productDao.save(product);
                        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_UPDATED_SUCCESSFULLY, HttpStatus.OK);
                    }
                    else {
                        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_NOT_FOUND, HttpStatus.OK);
                    }
                }
                else {
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            }
            else {
                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_NOT_ALLOWED_TO_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProductById(Integer id) {
        try {
            if(jwtFilter.isAdmin()) {
                Optional<Product> optionalProduct = productDao.findById(id);
                if(!optionalProduct.isEmpty()) {
                    productDao.deleteById(id);
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_DELETED_SUCCESSFULLY, HttpStatus.OK);
                }
                else {
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_NOT_FOUND, HttpStatus.OK);
                }
            }
            else {
                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_NOT_ALLOWED_TO_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProductStatus(Map<String,String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                Optional<Product> optionalProduct = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if(!optionalProduct.isEmpty()) {
                    productDao.updateStatus(Integer.parseInt(requestMap.get("id")), requestMap.get("status"));
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_STATUS_UPDATED_SUCCESSFULLY, HttpStatus.OK);
                }
                else {
                    return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.PRODUCT_NOT_FOUND, HttpStatus.OK);
                }
            }
            else {
                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.USER_NOT_ALLOWED_TO_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProductsByCategoryId(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getAllProductsByCategoryId(id),HttpStatus.OK);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProductsById(Integer id) {
        try {
            return new ResponseEntity<>(productDao.findAllById(id),HttpStatus.OK);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
