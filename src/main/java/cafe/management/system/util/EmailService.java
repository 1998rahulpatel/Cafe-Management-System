package cafe.management.system.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface EmailService {
    //send email to single person with cc all admin
    void sendEmail(String to, String subject, String text, List<String> cc);
    //send email to single person
    void sendEmail(String to, String subject, String message);
    //send email to multiple person
    void sendEmail(String[] to, String subject, String message);
    //send email with html
    void sendEmailWithHTML(String to, String subject, String htmlContent);
    //send email with file
    void sendEmailWithFile(String to, String subject, String message, File file);
    //send email with inputStream
    void sendEmailWithInputStream(String to, String subject, String message, InputStream inputStream);
}
