package com.microfinance.loan.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

	@Value("${jwt.secret:TXlEZWZhdWx0SldUU2VjcmV0S2V5Rm9yRGV2T25seTEyMzQ1Njc4OTA=}")
	private String secret;

	@Value("${jwt.expiration-ms:86400000}")
	private long expirationMs;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username != null
				&& username.equals(userDetails.getUsername())
				&& !isTokenExpired(token);
	}

	public String generateToken(String subject) {
		return generateToken(subject, new HashMap<>());
	}

	public String generateToken(String subject, Map<String, Object> extraClaims) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.claims(extraClaims)
				.subject(subject)
				.issuedAt(now)
				.expiration(expiration)
				.signWith(getSigningKey())
				.compact();
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSigningKey() {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
		} catch (IllegalArgumentException ex) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}
}

