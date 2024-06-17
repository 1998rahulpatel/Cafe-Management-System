package cafe.management.system.jwt;

import cafe.management.system.constant.CafeManagementSystemConstant;
import cafe.management.system.util.CafeManagementSystemUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Service
public class JWTUtil {

	private final SecretKey secretKey = Keys.hmacShaKeyFor("cafemangementsystemcafemangementsystemcafemangementsystemcafemangementsystem".getBytes());
	private final Integer tokenExpirationInMS = 1000 * 60 * 60 * 1;

	public String getJwtFromHeader(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		String token = authorizationHeader != null && authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7).trim() : null;
		log.info("Extracted JWT: {}", token);
		if (Objects.isNull(token)){
			log.error("Authorization header is missing or does not start with Bearer.");
		}
		return token;
	}

	public Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsername(String token) {
		return extractClaims(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaims(token, Claims::getExpiration);
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private String createToken(Map<String, Object> claims, String username) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenExpirationInMS))
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateToken(String username, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		return createToken(claims, username);
	}
}
