
package com.bookurmedical.controller;

import com.bookurmedical.entity.User;
import com.bookurmedical.service.UserService;
import com.bookurmedical.dto.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.bookurmedical.service.AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAdminUsers() {
        List<String> adminRoles = Arrays.asList("ADMIN", "SUPER_ADMIN", "OPERATIONS", "FINANCE");
        return ResponseEntity.ok(userService.getUsersByRoles(adminRoles));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<java.util.Map<String, Object>>> getPatients() {
        return ResponseEntity.ok(adminService.getPatients());
    }

    @GetMapping("/patients/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getPatientProfile(@PathVariable String id) {
        try {
            return ResponseEntity.ok(adminService.getPatientProfile(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createAdminUser(@RequestBody SignupRequest signupRequest) {
        try {
            userService.registerUser(signupRequest);
            return ResponseEntity.ok("Admin user created successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> deleteAdminUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
