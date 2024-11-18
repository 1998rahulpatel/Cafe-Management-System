package cafe.management.system.controller;

import cafe.management.system.model.Bill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RequestMapping("/bill")
public interface BillController {

    @PostMapping("/generate")
    public ResponseEntity<String> generateBill(@RequestBody Map<String, Object> requestMap);

    @GetMapping("/get/all")
    public ResponseEntity<List<Bill>> getBills();

    @PostMapping("/get/pdf")
    public ResponseEntity<byte []> getBill(@RequestBody Map<String, Object> requestMap);

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteBill(@PathVariable("id") Integer id);
}
