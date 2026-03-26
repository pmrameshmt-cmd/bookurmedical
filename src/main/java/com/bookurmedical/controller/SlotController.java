package com.bookurmedical.controller;

import com.bookurmedical.entity.Slot;
import com.bookurmedical.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private SlotRepository slotRepository;

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
        return ResponseEntity.ok("Slot selected successfully!");
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable String slotId) {
        slotRepository.deleteById(slotId);
        return ResponseEntity.ok("Slot deleted successfully!");
    }
}
