package cafe.management.system.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    // Regular expression for strong password
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +        // At least one digit
                    "(?=.*[a-z])" +         // At least one lowercase letter
                    "(?=.*[A-Z])" +         // At least one uppercase letter
                    "(?=.*[@#$%^&+=!])" +   // At least one special character
                    "(?=\\S+$)" +           // No whitespace
                    ".{8,12}$";             // Length between 8 and 12 characters

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}

