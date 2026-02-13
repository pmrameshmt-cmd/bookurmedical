package com.bookurmedical.restcontroller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomAccountService;
import com.bookurmedical.service.BloomUserService;


/**
 * @author RAM (16-06-2025)
 */

@RestController
@RequestMapping("/auth")
public class BloomAuth {

	@Autowired
	private BloomAccountService accountService;

	@Autowired
	private BloomUserService userService;

	@PostMapping(value = "/signup")
	public ResponseEntity<JSONObject> signup(@RequestBody JSONObject body) {
		return accountService.accountSignUp(body);
	}

	@PostMapping(value = "/login")
	public ResponseEntity<JSONObject> login(@RequestBody JSONObject body) {
		return userService.loginValidation(body);
	}
	
	@PostMapping(value = "/adduser")
	public ResponseEntity<JSONObject> adduser(@RequestBody JSONObject body) {
		userService.createUser(body.getString("shopNumber"), body, body.getString("role"));
		return ResponseEntity.ok().body(new JSONObject());
	}

}
