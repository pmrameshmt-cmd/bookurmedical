
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
                .filter(u -> "USER".equals(u.getRole()))
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
                patientMap.put("status", sheet.getStatus() != null ? sheet.getStatus() : "Pending");
                patientMap.put("treatment",
                        sheet.getPrimaryDiagnosis() != null ? sheet.getPrimaryDiagnosis() : "Not Specified");
                patientMap.put("forms", sheet.getStatus().equals("COMPLETED") ? 1 : 0);
            }, () -> {
                patientMap.put("status", "No Record");
                patientMap.put("treatment", "N/A");
                patientMap.put("forms", 0);
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
}
