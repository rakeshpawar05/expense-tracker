package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.SubscriptionDto;
import com.project.expenseTracker.dto.SubscriptionSummaryDto;
import com.project.expenseTracker.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<SubscriptionSummaryDto> getSubscriptions(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "yearMonth", required = false) YearMonth yearMonth) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionSummary(userId, yearMonth));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(@Valid @RequestBody SubscriptionDto dto) {
        return new ResponseEntity<>(subscriptionService.createSubscription(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionDto> updateSubscription(
            @PathVariable("id") Long id,
            @Valid @RequestBody SubscriptionDto dto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        subscriptionService.deleteSubscription(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<SubscriptionDto> toggleStatus(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId) {
        return ResponseEntity.ok(subscriptionService.toggleStatus(id, userId));
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<Map<String, Object>> logSubscriptionExpense(
            @PathVariable("id") Long id,
            @RequestParam("userId") Long userId,
            @RequestParam(name = "yearMonth", required = false) YearMonth yearMonth) {
        long expenseId = subscriptionService.logSubscriptionExpense(id, userId, yearMonth);
        return ResponseEntity.ok(Map.of(
                "expenseId", expenseId,
                "message", "Subscription logged as expense successfully"
        ));
    }
}
