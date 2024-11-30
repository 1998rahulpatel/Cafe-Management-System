package cafe.management.system.controllerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import cafe.management.system.dao.TokenDao;
import cafe.management.system.jwt.JWTUtil;
import cafe.management.system.model.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LogoutControllerImpl implements LogoutHandler{

    @Autowired
    JWTUtil jwtUtil;

    @Autowired
    TokenDao tokenDao;
    
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = this.jwtUtil.getJwtFromHeader(request);
        Token savedToken = this.tokenDao.findByToken(token).orElse(null);
        if (savedToken != null) {
            savedToken.setIsLoggedOut(true);
            this.tokenDao.save(savedToken);
        }
    }
}