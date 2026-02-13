package com.bookurmedical.service;

import java.util.Date;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * All jwt token related operations are performed here.
 * 
 * @author REVANTH (06-09-2024)
 * @since 1.0
 */
@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String jwtSecretKey;

	@Value("${security.jwt.accesstoken-expiration-time}")
	private long accessTokenExpirationTime;

	@Value("${security.jwt.refreshtoken-expiration-time}")
	private long refreshTokenExpirationTime;

	public String getJwtAccessToken(Map<String, Object> claims) {
		String jwt = Jwts.builder().issuer("auth")
				.expiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime)).issuedAt(new Date())
				.signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).claims(claims).compact();
		return jwt;
	}

	public String getJwtRefreshToken(Map<String, Object> claims) {
		String jwt = Jwts.builder().issuer("auth")
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime)).issuedAt(new Date())
				.signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).claims(claims).compact();
		return jwt;
	}

	public boolean isValidJwtToken(String jwt) {
		try {
			Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).build().parseSignedClaims(jwt)
					.getPayload();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public JSONObject getClaims(String jwt) {
		Claims claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes())).build()
				.parseSignedClaims(jwt).getPayload();
		return new JSONObject(claims);
	}
}
