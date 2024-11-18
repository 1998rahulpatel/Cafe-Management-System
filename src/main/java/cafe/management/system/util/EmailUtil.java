package cafe.management.system.util;

import cafe.management.system.constant.CafeManagementSystemConstant;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
public class EmailUtil implements EmailService {
    
    @Value("${spring.mail.username}")
    private String emailFrom;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String to, String subject, String text, List<String> cc){
        log.info("sending mail to user with cc all admin...");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(emailFrom);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        if(!cc.isEmpty()) {
            simpleMailMessage.setCc(getCCArray(cc));
        }
        javaMailSender.send(simpleMailMessage);
        log.info("mail sent...");
    }

    private String[] getCCArray(List<String> cc){
        String[] ccArray = new String[cc.size()];
        for(int i=0;i<cc.size();i++){
           ccArray[i] = cc.get(i);
        }
        return ccArray;
    }

    @Override
    public void sendEmail(String to, String subject, String message) {
        log.info("Sending mail to single person.");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(emailFrom);
        log.debug("simpleMailMessage  : {}",simpleMailMessage);
        javaMailSender.send(simpleMailMessage);
        log.info("Mail sent to single person.");
    }

    @Override
    public void sendEmail(String[] to, String subject, String message) {
        log.info("Sending mail to multiple person.");
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);
        simpleMailMessage.setFrom(emailFrom);
        log.debug("simpleMailMessage  : {}",simpleMailMessage);
        javaMailSender.send(simpleMailMessage);
        log.info("Mail sent to multiple person.");
    }

    @Override
    public void sendEmailWithHTML(String to, String subject, String htmlContent) {
        try {
            log.info("Sending mail with html.");
            MimeMessage simpleMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(simpleMailMessage,true,"UTF-8");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlContent,true);
            mimeMessageHelper.setFrom(emailFrom);
            log.debug("mimeMessageHelper  : {}",mimeMessageHelper);
            javaMailSender.send(simpleMailMessage);
            log.info("Mail sent with html.");
        }
        catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendEmailWithFile(String to, String subject, String message, File file) {
        try {
            log.info("Sending mail with file.");
            MimeMessage simpleMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(simpleMailMessage,true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(message);
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            mimeMessageHelper.addAttachment(fileSystemResource.getFilename(),file);
            mimeMessageHelper.setFrom(emailFrom);
            log.debug("mimeMessageHelper  : {}",mimeMessageHelper);
            javaMailSender.send(simpleMailMessage);
            log.info("Mail sent with file.");
        }
        catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendEmailWithInputStream(String to, String subject, String message, InputStream inputStream) {
        try {
            log.info("Sending mail with file.");
            MimeMessage simpleMailMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(simpleMailMessage,true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(message);
            File file = new File("src\\main\\resources\\email\\test.txt");
            try {
                Files.copy(inputStream,file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            FileSystemResource fileSystemResource = new FileSystemResource(file);
            mimeMessageHelper.addAttachment(fileSystemResource.getFilename(),file);
            mimeMessageHelper.setFrom(emailFrom);
            log.debug("mimeMessageHelper  : {}",mimeMessageHelper);
            javaMailSender.send(simpleMailMessage);
            log.info("Mail sent with file.");
        }
        catch (MessagingException ex) {
            ex.printStackTrace();
        }
    }

    public void approvalMail(String user, String subject, String approver) {
        String message = "Dear " + user + ",\n\n" +
                CafeManagementSystemConstant.ADMIN_APPROVED_ACCOUNT_MESSAGE + "(" + approver + ").\n\n" +
                "We are thrilled to have you on board.\n\n" + "Best regards,\n" +
                "The Cafe Management System Team";
        sendEmail(user,subject,message);
    }

    public void disableMail(String user, String subject, String approver) {
        String message = "Dear " + user + ",\n\n" +
                CafeManagementSystemConstant.ADMIN_DISABLED_ACCOUNT_MESSAGE + "(" + approver  + ").\n\n" +
                "We regret to inform you that your account has been disabled by the administrator\n\n" +
                "Best regards,\n" +
                "The Cafe Management System Team";
        sendEmail(user,subject,message);
    }

    public void forgotPasswordMail(String to, String subject, String password) throws MessagingException {
        String htmlContent = "<html>"
                + "<body>"
                + "<p>Dear "+ to +",</p>"
                + "<p>Your login details for the Cafe Management System are:</p>"
                + "<ul>"
                + "<li><b>Email:</b> " + to + "</li>"
                + "<li><b>Password:</b> " + password + "</li>"
                + "</ul>"
                + "<p>Please use the following link to login:</p>"
                + "<p><a href='http://localhost:4200'>Click here to login</a></p>"
                + "<p>Best regards,<br/>"
                + "Your Cafe Management System Team</p>"
                + "</body>"
                + "</html>";
        sendEmailWithHTML(to,subject,htmlContent);
    }
}
