package cafe.management.system.controllerImpl;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.controller.CategoryController;
import cafe.management.system.model.Category;
import cafe.management.system.service.CategoryService;
import cafe.management.system.util.CafeManagementSystemUtil;
import cafe.management.system.wrapper.UserWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class CategoryControllerImpl implements CategoryController {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<String> addNewCategory(@RequestBody(required = true) Map<String, String> requestMap) {
        try {
            return categoryService.addNewCategory(requestMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String categoryName) {
        try {
            return categoryService.getAllCategory(categoryName);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try{
            return categoryService.updateCategory(requestMap);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
