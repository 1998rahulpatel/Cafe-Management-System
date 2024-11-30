package cafe.management.system.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import cafe.management.system.dao.TokenDao;
import cafe.management.system.model.Token;
import cafe.management.system.model.User;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Component
public class JWTUtil {

	@Autowired
	TokenDao tokenDao;

	@Value("${jwtSecretKey}")
	private String jwtSecretKey;
	@Value("${jwtExpirationInHS}")
	private Long jwtExpirationInHS;
	private SecretKey secretKey;
	private long tokenExpirationInMS;

	@PostConstruct
	public void init() {
		if (jwtSecretKey.length() < 32) {
			throw new IllegalArgumentException("JWT secret key must be at least 32 characters long.");
		}
		this.secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
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
		boolean isValidToken = this.tokenDao.findByToken(token).map(t -> !t.getIsLoggedOut()).orElse(false);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && isValidToken);
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

	public String generateTokenAndStoreInDB(User user){
		String jwtToken = generateToken(user.getEmail(), user.getRole());
		Token token = new Token();
		token.setUserId(user.getId());
		token.setIsLoggedOut(false);
		token.setToken(jwtToken);
		this.tokenDao.save(token);
		return jwtToken;
	}

	public void revokeAllTokensByUser(User user) {
		List<Token> validTokenListByUser = this.tokenDao.findAllTokensByUser(user.getId());
		
		if (validTokenListByUser != null && !validTokenListByUser.isEmpty()) {
			validTokenListByUser.forEach(token -> token.setIsLoggedOut(true));
			this.tokenDao.saveAll(validTokenListByUser);
		}
	}
}
