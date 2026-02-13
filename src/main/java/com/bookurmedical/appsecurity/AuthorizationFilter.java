package com.bookurmedical.appsecurity;

import java.util.Collections;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bookurmedical.service.BloomUserService;
import com.bookurmedical.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Authorization of all api calls are done here.If authentication is not set in
 * SecurityContextHolder, api request won't be send to the restcontroller,
 * spring
 * handles and returns 403 response unless it is publicPaths allowed uri in
 * SecurityConfig
 * 
 * @author REVANTH (16-11-2024)
 * @since 1.0
 */

@Component
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

	@Value("${application.app.key}")
	private String appKey;

	@Value("${application.app.password}")
	private String appPassword;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private BloomUserService bloomUserService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, java.io.IOException {
		try {

			String authHeader = request.getHeader("Authorization");
			String appSecretPassword = request.getHeader(appKey);
			String shopNumber = request.getHeader("shopNumber");
			if (authHeader != null && authHeader.startsWith("Bearer ")
					&& SecurityContextHolder.getContext().getAuthentication() == null) {
				String token = authHeader.substring(7);
				if (!jwtService.isValidJwtToken(token)) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
					return;
				}
				JSONObject claims = jwtService.getClaims(authHeader.substring(7));

				if (claims.has("email") && claims.getString("email") != null) {
					Document userDocument = bloomUserService.getSingleUserByEmail(claims.getString("email"));
					if (userDocument != null
							&& userDocument.getString("shopNumber").equals(claims.getString("shopNumber"))) {

						UserDetails userDetails = User.withUsername(claims.getString("email")).password("")
								.authorities(Collections.singletonList(new SimpleGrantedAuthority("USER"))).build();
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						SecurityContextHolder.getContext().setAuthentication(authentication);
						request.setAttribute("shopNumber", claims.getString("shopNumber"));
						request.setAttribute("user", claims.getString("email"));
						filterChain.doFilter(request, response);
						return;
					} else {
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
						return;
					}
				}
			} else if (appSecretPassword != null && appSecretPassword.equals(appPassword) && shopNumber != null) {
				UserDetails userDetails = User.withUsername("SYSTEM").password("")
						.authorities(Collections.singletonList(new SimpleGrantedAuthority("SYSTEM"))).build();
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				request.setAttribute("shopNumber", shopNumber);
				request.setAttribute("user", "SYSTEM");
				filterChain.doFilter(request, response);
				return;
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setContentType("application/json"); // Set content type to JSON
			response.getWriter().write("{\"error\": \"Internal server error\"}");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		filterChain.doFilter(request, response);
	}
}
