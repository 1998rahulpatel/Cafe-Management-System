package cafe.management.system.jwt;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.util.CafeManagementSystemUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter {

	@Autowired
	private JWTUtil jwtUtil;

	@Autowired
	private CustomUsersDetailsService customUsersDetailsService;

	Claims claims = null;
	private String username = null;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		log.info("doInternalFilter called for URI: {}", request.getRequestURI());

		if (request.getServletPath().matches("/user/login|/user/forgotPassword")) {
			filterChain.doFilter(request, response);
		} else {
			try {
				String token = jwtUtil.getJwtFromHeader(request);

				if (token != null) {
					username = jwtUtil.extractUsername(token);
					claims = jwtUtil.extractAllClaims(token);
				}
				else
				{
					log.error(CafeManagementSystemConstant.INVALID_JWT_SUPPLIED);
				}

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = customUsersDetailsService.loadUserByUsername(username);

					if (jwtUtil.validateToken(token, userDetails)) {
						UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
						usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
					}
				}
			} catch (Exception ex) {
				log.error("Cannot set user authentication.", ex);
			}
			filterChain.doFilter(request, response);
		}
	}

	public boolean isAdmin() {
		return "admin".equalsIgnoreCase((String) claims.get("role"));
	}

	public boolean isUser() {
		return "user".equalsIgnoreCase((String) claims.get("role"));
	}

	public String getCurrent() {
		return username;
	}
}
