package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.AICategoryInsightDto;
import com.project.expenseTracker.dto.AIInsightDto;
import com.project.expenseTracker.dto.AIParseExpenseRequest;
import com.project.expenseTracker.dto.AIRequest;
import com.project.expenseTracker.dto.ParsedExpenseDto;
import com.project.expenseTracker.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

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

    @GetMapping("/monthly-insight")
    public ResponseEntity<AIInsightDto> getMonthlyInsight(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "yearMonth", required = false) YearMonth yearMonth) {
        AIInsightDto insight = aiService.getMonthlyInsight(userId, yearMonth);
        return ResponseEntity.ok(insight);
    }

    @PostMapping("/parse-expense")
    public ResponseEntity<ParsedExpenseDto> parseExpense(@RequestBody AIParseExpenseRequest request) {
        ParsedExpenseDto parsed = aiService.parseExpense(request);
        return ResponseEntity.ok(parsed);
    }

    @GetMapping("/category-insight")
    public ResponseEntity<AICategoryInsightDto> getCategoryInsight(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "categoryName", required = false) String categoryName,
            @RequestParam(name = "yearMonth", required = false) YearMonth yearMonth) {
        AICategoryInsightDto insight = aiService.getCategoryInsight(userId, categoryId, categoryName, yearMonth);
        return ResponseEntity.ok(insight);
    }
}
