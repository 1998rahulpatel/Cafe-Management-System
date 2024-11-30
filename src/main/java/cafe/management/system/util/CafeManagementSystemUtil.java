package cafe.management.system.util;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CafeManagementSystemUtil {

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body("{\"message\":\"" + responseMessage + "\"}");
    }

    public static String generateUUID() {
        Date date = new Date();
        Long time = date.getTime();
        return "BillReceipt-"+time;
    }

    public static JSONArray getJSONArrayFromString(String data) throws JSONException {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(data);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonArray;
    }
    public static Map<String, Object> getMapFromJSON(String data) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        if(!Strings.isNullOrEmpty(data)){
            return new Gson().fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
        }
        return map;
    }

    public static Boolean isFileExist(String filePath) {
        log.debug("Inside isFileExist method. | filePath: {}" + filePath);
        try{
            File file = new File(filePath);
            return file != null && file.exists() ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Boolean.FALSE;
    }
}
