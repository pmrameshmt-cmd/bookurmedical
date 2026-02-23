
package com.bookurmedical.controller;

import com.bookurmedical.entity.MedicalCaseSheet;
import com.bookurmedical.entity.User;
import com.bookurmedical.repository.MedicalCaseSheetRepository;
import com.bookurmedical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/medical-history")
public class MedicalHistoryController {

    @Autowired
    MedicalCaseSheetRepository medicalCaseSheetRepository;

    @Autowired
    UserRepository userRepository;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public MedicalHistoryController() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitMedicalHistory(@RequestBody MedicalCaseSheet medicalCaseSheet) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        medicalCaseSheet.setStatus("COMPLETED");
        medicalCaseSheet.setUserId(user.getId());

        // Upsert logic: if a sheet already exists for this user, update it
        medicalCaseSheetRepository.findByUserId(user.getId()).ifPresent(existing -> {
            medicalCaseSheet.setId(existing.getId());
        });

        medicalCaseSheetRepository.save(medicalCaseSheet);

        // Update user profile status
        user.setProfileCompleted(true);
        userRepository.save(user);

        return ResponseEntity.ok("Medical history submitted successfully!");
    }

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(@RequestBody MedicalCaseSheet medicalCaseSheet) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        medicalCaseSheet.setStatus("DRAFT");
        medicalCaseSheet.setUserId(user.getId());

        // Upsert logic: if a sheet/draft already exists for this user, update it
        medicalCaseSheetRepository.findByUserId(user.getId()).ifPresent(existing -> {
            medicalCaseSheet.setId(existing.getId());
        });

        medicalCaseSheetRepository.save(medicalCaseSheet);

        return ResponseEntity.ok("Draft saved successfully!");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Normalize file name
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            return ResponseEntity.ok(fileName);
        } catch (IOException ex) {
            return ResponseEntity.badRequest()
                    .body("Could not upload file " + file.getOriginalFilename() + ". Please try again!");
        }
    }
}
