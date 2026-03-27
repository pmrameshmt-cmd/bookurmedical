
package com.bookurmedical.service;

import com.bookurmedical.entity.User;
import com.bookurmedical.repository.MedicalCaseSheetRepository;
import com.bookurmedical.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicalCaseSheetRepository medicalCaseSheetRepository;

    public List<Map<String, Object>> getPatients() {
        // Fetch all users with role 'USER'
        List<User> users = userRepository.findAll().stream()
                .filter(u -> "USER".equalsIgnoreCase(u.getRole()) || "PATIENT".equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> patientMap = new HashMap<>();
            patientMap.put("id", user.getId());
            patientMap.put("name", (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                    (user.getLastName() != null ? user.getLastName() : ""));
            patientMap.put("email", user.getEmail());
            // Default joined date if not available
            patientMap.put("joined", "2024-02-28");

            // Try to find medical case sheet
            medicalCaseSheetRepository.findByUserId(user.getId()).ifPresentOrElse(sheet -> {
                patientMap.put("caseId", sheet.getId());
                patientMap.put("status", sheet.getStatus() != null ? sheet.getStatus() : "NEW");
                patientMap.put("treatment",
                        sheet.getPrimaryDiagnosis() != null ? sheet.getPrimaryDiagnosis() : "General Consultation");
                patientMap.put("forms", "COMPLETED".equalsIgnoreCase(sheet.getStatus()) ? 1 : 0);
                patientMap.put("doctorId", sheet.getAssignedDoctorId());
                
                if (sheet.getAssignedDoctorId() != null) {
                    userRepository.findById(sheet.getAssignedDoctorId()).ifPresent(d -> {
                        String drName = "Dr. " + (d.getFirstName() != null ? d.getFirstName() : "") + " " + (d.getLastName() != null ? d.getLastName() : "");
                        patientMap.put("assignedDoctor", drName.trim());
                    });
                } else {
                    patientMap.put("assignedDoctor", null);
                }
            }, () -> {
                patientMap.put("caseId", null);
                patientMap.put("status", "NEW");
                patientMap.put("treatment", "Not Started");
                patientMap.put("forms", 0);
                patientMap.put("doctorId", null);
                patientMap.put("assignedDoctor", null);
            });

            result.add(patientMap);
        }

        return result;
    }

    public Map<String, Object> getPatientProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        Map<String, Object> profile = new HashMap<>();
        profile.put("user", user);

        medicalCaseSheetRepository.findByUserId(userId).ifPresentOrElse(sheet -> {
            profile.put("medicalData", sheet);
        }, () -> {
            profile.put("medicalData", null);
        });

        return profile;
    }

    public void assignDoctor(String patientId, String doctorId) {
        // Find the completed medical case sheet for this user
        medicalCaseSheetRepository.findByUserId(patientId).ifPresentOrElse(sheet -> {
            sheet.setAssignedDoctorId(doctorId);
            // Synchronize with Frontend Workflow Stages
            sheet.setStatus("Doctor Assigned"); 
            medicalCaseSheetRepository.save(sheet);
        }, () -> {
            throw new RuntimeException("Medical history not found for patient " + patientId);
        });
    }
}
