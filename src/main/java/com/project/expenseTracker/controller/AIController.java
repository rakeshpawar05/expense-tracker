package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.AIRequest;
import com.project.expenseTracker.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody AIRequest request) {
        String response = aiService.processQuery(request);
        return ResponseEntity.ok(response);
    }
}