package com.bookurmedical.controller;

import com.bookurmedical.entity.Slot;
import com.bookurmedical.entity.MedicalCaseSheet;
import com.bookurmedical.repository.SlotRepository;
import com.bookurmedical.repository.MedicalCaseSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private SlotRepository slotRepository;
 
    @Autowired
    private MedicalCaseSheetRepository caseRepository;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Slot>> getDoctorSlots(@PathVariable String doctorId) {
        return ResponseEntity.ok(slotRepository.findByDoctorId(doctorId));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<Slot>> getCaseSlots(@PathVariable String caseId) {
        return ResponseEntity.ok(slotRepository.findByCaseId(caseId));
    }

    @PostMapping("/share")
    public ResponseEntity<List<Slot>> shareSlots(@RequestBody List<Slot> slots) {
        return ResponseEntity.ok(slotRepository.saveAll(slots));
    }

    @PostMapping("/{slotId}/select")
    public ResponseEntity<?> selectSlot(@PathVariable String slotId, @RequestParam String patientId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Error: Slot not found."));
        slot.setPatientId(patientId);
        slot.setStatus("selected");
        slotRepository.save(slot);
 
        // ── Backend Sync: Update Clinical Case Status ────────────────────────────
        if (slot.getCaseId() != null) {
            Optional<MedicalCaseSheet> caseSheet = caseRepository.findById(slot.getCaseId());
            if (caseSheet.isPresent()) {
                MedicalCaseSheet sheet = caseSheet.get();
                sheet.setStatus("Slot Selected");
                caseRepository.save(sheet);
            }
        }
 
        return ResponseEntity.ok("Slot selected successfully!");
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable String slotId) {
        slotRepository.deleteById(slotId);
        return ResponseEntity.ok("Slot deleted successfully!");
    }
}
