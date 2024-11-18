package cafe.management.system.serviceImp;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.dao.CategoryDao;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.model.Category;
import cafe.management.system.service.CategoryService;
import cafe.management.system.util.CafeManagementSystemUtil;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()) {
                if(validateCategoryMap(requestMap,false)) {
                   categoryDao.save(getCategoryFromMap(requestMap, false));
                   return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.CATEGORY_ADDED_SUCCESSFULLY, HttpStatus.OK);
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

    private boolean validateCategoryMap(Map<String, String> requestMap, Boolean validateId) {
        Category category = new Category();
        if(requestMap.containsKey("name")) {
            if(requestMap.containsKey("id") && validateId) {
               return true;
            }
            else if (!validateId) {
                category.setName(requestMap.get("name"));
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdmin) {
       Category category = new Category();
       if (isAdmin && requestMap.containsKey("id") && requestMap.get("id") != null) {
           category.setId(Integer.parseInt(requestMap.get("id")));
       }
        category.setName(requestMap.get("name"));
       return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true")) {
                return new ResponseEntity<List<Category>>(categoryDao.getAllCategory(), HttpStatus.OK);
            }
            log.info("Fetching all categories");
            return new ResponseEntity<List<Category>>(categoryDao.findAll(), HttpStatus.OK);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()){
                 if(validateCategoryMap(requestMap,true)) {
                    Optional<Category> categoryOptional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!categoryOptional.isEmpty()) {
                         categoryDao.save(getCategoryFromMap(requestMap, true));
                         return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.CATEGORY_UPDATED_SUCCESSFULLY, HttpStatus.OK);
                    }
                    else {
                         return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.CATEGORY_NOT_FOUND, HttpStatus.OK);
                     }
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
}