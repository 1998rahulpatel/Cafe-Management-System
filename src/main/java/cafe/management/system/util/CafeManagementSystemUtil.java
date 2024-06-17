package cafe.management.system.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CafeManagementSystemUtil {

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body("{\"message\":\"" + responseMessage + "\"}");
    }
}
