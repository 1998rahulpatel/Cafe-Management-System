package cafe.management.system.serviceImpl;

import cafe.management.system.dao.BillDao;
import cafe.management.system.dao.CategoryDao;
import cafe.management.system.dao.ProductDao;
import cafe.management.system.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<Map<String, Object>> getDetails() {
        try {
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("category", categoryDao.count());
            detailsMap.put("product", productDao.count());
            detailsMap.put("bill", billDao.count());
            return new ResponseEntity<>(detailsMap, HttpStatus.OK);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
