
package com.bookurmedical.controller;

import com.bookurmedical.entity.MedicalCaseSheet;
import com.bookurmedical.entity.User;
import com.bookurmedical.repository.MedicalCaseSheetRepository;
import com.bookurmedical.repository.UserRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/medical-history")
public class MedicalHistoryController {

    @Autowired
    MedicalCaseSheetRepository medicalCaseSheetRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations gridFsOperations;

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
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Store file in MongoDB GridFS
            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    fileName,
                    file.getContentType());

            // Return the filename (used as the key stored in MedicalCaseSheet fields)
            return ResponseEntity.ok(fileName);
        } catch (IOException ex) {
            return ResponseEntity.badRequest()
                    .body("Could not upload file " + file.getOriginalFilename() + ". Please try again!");
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        try {
            // Find file in GridFS by filename
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    new Query(Criteria.where("filename").is(fileName)));

            if (gridFSFile == null) {
                return ResponseEntity.notFound().build();
            }

            // Get the file content stream
            InputStream inputStream = gridFsOperations.getResource(gridFSFile).getInputStream();

            // Determine content type
            String contentType = "application/octet-stream";
            if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().get("_contentType") != null) {
                contentType = gridFSFile.getMetadata().get("_contentType").toString();
            }

            // Extract the display name (part after UUID_)
            String displayName = fileName;
            int underscoreIdx = fileName.indexOf('_');
            if (underscoreIdx > 0 && underscoreIdx < fileName.length() - 1) {
                displayName = fileName.substring(underscoreIdx + 1);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + displayName + "\"")
                    .body(new InputStreamResource(inputStream));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("Error downloading file: " + ex.getMessage());
        }
    }
}
