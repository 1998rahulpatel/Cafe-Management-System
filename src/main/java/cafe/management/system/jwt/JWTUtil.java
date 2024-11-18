package cafe.management.system.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${jwtSecretKey}")
	private String jwtSecretKey;
	@Value("${jwtExpirationInHS}")
	private Long jwtExpirationInHS;
	private SecretKey secretKey;
	private long tokenExpirationInMS;

	@PostConstruct
	public void init() {
		// Initialize the SecretKey in a @PostConstruct method
		secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
		tokenExpirationInMS = 1000 * 60 * 60 * jwtExpirationInHS;
		log.info("JWT SecretKey and ExpirationTime initialized successfully.");
	}



	public String getJwtFromHeader(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7).trim();
		} else {
			log.error("Authorization header is missing or does not start with Bearer.");
			return null;
		}
	}

	public Claims extractAllClaims(String token) {
		if (!Objects.isNull(token)) {
			log.debug("Extracting all claims from token: {}", token);
		}
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

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	private String createToken(Map<String, Object> claims, String username) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date())
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
