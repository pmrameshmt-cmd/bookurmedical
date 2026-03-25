package com.bookurmedical.controller;

import com.bookurmedical.entity.Message;
import com.bookurmedical.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/{caseId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String caseId) {
        return ResponseEntity.ok(messageRepository.findByCaseIdOrderByTimestampAsc(caseId));
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        // In a real app, we'd validate the senderId against the SecurityContext
        return ResponseEntity.ok(messageRepository.save(message));
    }
}
