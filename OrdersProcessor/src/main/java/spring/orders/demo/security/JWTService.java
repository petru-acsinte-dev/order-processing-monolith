package spring.orders.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

	private final SecretKey secretKey;

	// represented in ms
	private final long expiration;

	public JWTService(@Value("${jwt.secret}") String secret,
			@Value("${expiration}") long expiration) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiration = expiration;
	}

	public String generateToken(UserDetails userDetails) {
		final Date created = new Date();
		return Jwts.builder()
			.subject(userDetails.getUsername())
			.issuedAt(created)
			.expiration(new Date(created.getTime() + expiration))
			.signWith(secretKey)
			.compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final boolean expired = isTokenExpired(token);
		if (expired) {
			return false;
		}
		final String username = getUsername(token);
		return username.equals(userDetails.getUsername());
	}

	public String getUsername(String token) {
		final Claims claims = extractClaims(token);
		return claims.getSubject();
	}

	private boolean isTokenExpired(String token) {
		final Claims claims = extractClaims(token);
		return claims.getExpiration().before(new Date());
	}

	private Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
