package cafe.management.system.serviceImpl;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.dao.BillDao;
import cafe.management.system.jwt.JWTFilter;
import cafe.management.system.model.Bill;
import cafe.management.system.service.BillService;
import cafe.management.system.util.CafeManagementSystemUtil;
import com.google.common.base.Strings;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    private BillDao billDao;

    @Autowired
    JWTFilter jwtFilter;

    @Override
    public ResponseEntity<String> generateBill(Map<String, Object> requestMap) {
        log.debug("Inside generateBill method. requestMap: {}", requestMap);
        try {
            String fileName;
            if(validateRequestMap(requestMap)){
                if(requestMap.containsKey("isGenerate") && !(Strings.isNullOrEmpty((String) requestMap.get("isGenerate")))){
                    fileName = (String) requestMap.get("uuid");
                }
                else{
                    fileName = CafeManagementSystemUtil.generateUUID();
                    requestMap.put("uuid", fileName);
                    saveBillDetailsInDB(requestMap);
                }

                generateBillInPdf(requestMap, fileName);

                return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BILL_GENERATE_SUCCESSFULLY + fileName,HttpStatus.OK);
            }
            return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BILL_REQUIRED_DATA_NOT_FOUND,HttpStatus.BAD_REQUEST);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void saveBillDetailsInDB(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Double.parseDouble((String)requestMap.get("total")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrent());
            billDao.save(bill);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
		return requestMap.containsKey("name") && requestMap.containsKey("email")
				&& requestMap.containsKey("contactNumber") && requestMap.containsKey("paymentMethod")
				&& requestMap.containsKey("total") && requestMap.containsKey("productDetails");
    }

    private void generateBillInPdf(Map<String, Object> requestMap, String fileName) {
        log.info("Inside generateBillInPdf method");
        try {
            String data = "Name: " + requestMap.get("name") + "\n" +
                    "Email: " + requestMap.get("email") + "\n" +
                    "Contact Number: " + requestMap.get("contactNumber") + "\n" +
                    "Payment Method: " + requestMap.get("paymentMethod");

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(CafeManagementSystemConstant.BILL_PATH + "\\" + fileName + ".pdf"));
            document.open();

            setRectangleInPdf(document);

            log.debug("Adding header to the PDF");
            Paragraph headerParagraph = new Paragraph("Cafe Management System",getFont("Header"));
            headerParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(headerParagraph);

            log.debug("Adding data to the PDF");
            Paragraph dataParagraph = new Paragraph(data + "\n \n",getFont("Data"));
            document.add(dataParagraph);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addTableHeader(table);

            JSONArray jsonArray = CafeManagementSystemUtil.getJSONArrayFromString((String) requestMap.get("productDetails"));
            for(int i =0; i < jsonArray.length(); i++){
                addRows(table, CafeManagementSystemUtil.getMapFromJSON(jsonArray.getString(i)));
            }
            document.add(table);

            log.debug("Adding footer to the PDF");
            Paragraph footerParagraph = new Paragraph("Total: " + requestMap.get("total") + "\n" +
                    "Thank you for visiting. Please visit again.",getFont("Data"));
            document.add(footerParagraph);
            document.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf method");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type) throws DocumentException {
        log.info("Inside getFont method");
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.COURIER, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader method");
        Stream.of("Product Name","Category","Quantity","Price","Total").forEach(columnTitle -> {
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            headerCell.setBorderWidth(2);
            headerCell.setPhrase(new Phrase(columnTitle));
            headerCell.setBackgroundColor(BaseColor.YELLOW);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(headerCell);
        });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows method");
        table.addCell((String)data.get("name"));
        table.addCell((String)data.get("category"));
        table.addCell((String)data.get("quantity"));
        table.addCell(String.valueOf((Double)data.get("price")));
        table.addCell(String.valueOf((Double)data.get("total")));
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> billList = new ArrayList<>();
        try {
            if(jwtFilter.isAdmin()){
                billList = billDao.findAll();
            }
            else{
                billList = billDao.findAllByEmail(jwtFilter.getCurrent());
            }
            return new ResponseEntity<>(billList, HttpStatus.OK);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.debug("Inside getPdf method. | requestMap: {}" + requestMap);
        try {
            byte[] byteArray = new byte[0];
            if (!requestMap.containsKey("uuid") && validateRequestMap(requestMap)){
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = CafeManagementSystemConstant.BILL_PATH + "\\" + (String)requestMap.get("uuid") + ".pdf";
            if (CafeManagementSystemUtil.isFileExist(filePath)){
                log.info("File exists, returning it. | filePath: {}" + filePath);
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
            else {
                log.info("File does not exist, generating a new one and returning it. | filePath: {}" + filePath);
                requestMap.put("isGenerate","false");
                generateBill(requestMap);
                byteArray = getByteArray(filePath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getByteArray(String filePath) throws Exception {
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        inputStream.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional<Bill> optionalBill = billDao.findById(id);
            if (!optionalBill.isEmpty()) {
                billDao.deleteById(id);return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BILL_DELETED_SUCCESSFULLY, HttpStatus.OK);
            }
            return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.BILL_DOES_NOT_EXIST, HttpStatus.OK);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeManagementSystemUtil.getResponseEntity(CafeManagementSystemConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
