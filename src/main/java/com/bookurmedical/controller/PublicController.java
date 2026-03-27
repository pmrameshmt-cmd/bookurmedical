
package com.bookurmedical.controller;

import com.bookurmedical.entity.User;
import com.bookurmedical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private UserService userService;

    @GetMapping("/doctors")
    public ResponseEntity<List<User>> getDoctors() {
        // Fetch all users with DOCTOR or MODERATE_DOCTOR roles
        List<User> doctors = userService.getUsersByRoles(Arrays.asList("DOCTOR", "MODERATE_DOCTOR"));
        return ResponseEntity.ok(doctors);
    }
}
