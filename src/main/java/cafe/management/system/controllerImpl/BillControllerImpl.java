package cafe.management.system.controllerImpl;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.controller.BillController;
import cafe.management.system.model.Bill;
import cafe.management.system.service.BillService;
import cafe.management.system.util.CafeManagementSystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class BillControllerImpl implements BillController {

    @Autowired
    BillService billService;

    @Override
    public ResponseEntity<String> generateBill(Map<String, Object> requestMap) {
        try {
            return billService.generateBill(requestMap);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        try {
            return billService.getBills();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte []> getBill(Map<String, Object> requestMap) {
        try {
            return billService.getPdf(requestMap);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            return billService.deleteBill(id);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
