package com.bookurmedical.service;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bookurmedical.database.BloomAccountDatabase;

@Service
public class BloomUserService {

	@Autowired
	BloomAccountDatabase accountDatabase;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	private String hashPassword(String plainPassword) {
		return passwordEncoder.encode(plainPassword);
	}

	private boolean checkPassword(String plainPassword, String hashedPassword) {
		return passwordEncoder.matches(plainPassword, hashedPassword);
	}

	public Document createUser(String shopNumber, JSONObject jsonObject, String userType) {
		Document userDocument = new Document();

		Document credentials = new Document();
		credentials.put("password", hashPassword(jsonObject.getString("password")));

		userDocument.put("shopNumber", shopNumber);
		userDocument.put("name", jsonObject.getString("name"));
		userDocument.put("email", jsonObject.getString("email"));
		userDocument.put("credentials", credentials);
		userDocument.put("role", userType);
		userDocument.put("createdAt", System.currentTimeMillis() / 1000L);
		userDocument.put("updatedAt", System.currentTimeMillis() / 1000L);
		accountDatabase.createUser(userDocument);
		return userDocument;
	}

	public ResponseEntity<JSONObject> loginValidation(JSONObject jsonRequest) {
		JSONObject response = new JSONObject();
		try {
			if (!jsonRequest.has("email") || !jsonRequest.has("password")) {
				response.put("status", "error");
				response.put("errorMessage", "Invalid credentials");
				return ResponseEntity.badRequest().body(response);
			}
			Document searchQuery = new Document();
			Document projection = new Document();
			searchQuery.put("email", jsonRequest.getString("email"));
			Document userDocument = accountDatabase.getSingleUser(searchQuery, projection);
			System.out.println("login email : " + jsonRequest.getString("email"));
			if (userDocument == null || userDocument.isEmpty()) {
				response.put("error", "User not found");
				return ResponseEntity.badRequest().body(response);
			}
			if (!checkPassword(jsonRequest.getString("password"),
					((Document) userDocument.get("credentials")).getString("password"))) {
				response.put("error", "Wrong password");
				return ResponseEntity.badRequest().body(response);
			}

			Map<String, Object> claims = new HashMap<>();
			claims.put("shopNumber", userDocument.getString("shopNumber"));
			claims.put("name", userDocument.getString("name"));
			claims.put("email", userDocument.getString("email"));
			String accessToken = jwtService.getJwtAccessToken(claims);
			String refreshToken = jwtService.getJwtRefreshToken(claims);

			response.put("accessToken", accessToken);
			response.put("refreshToken", refreshToken);
			response.put("name", userDocument.getString("name"));
			ObjectId id = userDocument.getObjectId("_id");
			response.put("userID", id.toHexString());

			// Fetch account details from account collection
			Document accountSearchQuery = new Document();
			accountSearchQuery.put("shopNumber", userDocument.getString("shopNumber"));
			Document accountProjection = new Document();
			accountProjection.put("shopName", 1);
			accountProjection.put("address", 1);
			accountProjection.put("phone", 1);
			Document accountDocument = accountDatabase.getSingleAccount(accountSearchQuery, accountProjection);

			// Add account details to response
			if (accountDocument != null && !accountDocument.isEmpty()) {
				response.put("shopName", accountDocument.getString("shopName"));
				response.put("address", accountDocument.getString("address"));
				response.put("phone", accountDocument.getString("phone"));
			}

			Document updateQuery = new Document();
			Document setQuery = new Document();
			setQuery.put("lastLoggedAt", System.currentTimeMillis() / 1000L);
			setQuery.put("refreshToken", refreshToken);
			updateQuery.put("$set", setQuery);
			accountDatabase.updateSingleUser(searchQuery, updateQuery);
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			response = new JSONObject().put("status", "error");
			response.put("errorMessage", "Internal Server Error");
			return ResponseEntity.internalServerError().body(response);
		}

	}

	public Document getSingleUserByEmail(String email) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		projection.put("credentials", 0);
		return accountDatabase.getSingleUser(searchQuery, projection);
	}

	public Document getSingleUser(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return accountDatabase.getSingleUser(searchQuery, projection);
	}

}
