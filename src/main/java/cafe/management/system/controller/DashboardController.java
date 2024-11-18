package cafe.management.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@CrossOrigin(origins = "*")
@RequestMapping("/dashboard")
public interface DashboardController {

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getDetails();
}
