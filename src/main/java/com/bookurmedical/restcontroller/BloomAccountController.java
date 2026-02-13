package com.bookurmedical.restcontroller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomAccountService;

@RestController
@RequestMapping("/account")
public class BloomAccountController {

    @Autowired
    private BloomAccountService accountService;

    @GetMapping("/getAccountInfo")
    public ResponseEntity<JSONObject> getAccountInfo(
            @RequestAttribute(value = "shopNumber", required = true) String shopNumber,
            @RequestAttribute(value = "user", required = true) String userEmail) {
        return accountService.getAccountInfo(shopNumber, userEmail);
    }
}
