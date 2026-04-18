package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.AIRequest;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.IntentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ExpenseService expenseService;

    private final AIIntentService aiIntentService;

    public String processQuery(AIRequest request) {

        IntentResult intent = aiIntentService.detect(request.getMessage());

        return switch (intent.getIntent()) {
            case TOTAL_EXPENSE -> getTotalExpense(request.getUserId(), YearMonth.parse(intent.getYearMonth()));
            case TOP_EXPENSES -> getTopExpenses(request.getUserId(), YearMonth.parse(intent.getYearMonth()));
            case CATEGORY_SUMMARY -> getCategorySummary(request.getUserId(), YearMonth.parse(intent.getYearMonth()));
            default -> "I didn't understand that. Try asking about expenses.";
        };
    }

    private String getTotalExpense(Long userId, YearMonth yearMonth) {

        List<ExpenseDto> expenses =
                expenseService.getExpenses(userId, yearMonth, null, null);

        int total = expenses.stream()
                .mapToInt(ExpenseDto::getAmount)
                .sum();

        return "You spent ₹" + total + " in " + yearMonth;
    }

    private String getTopExpenses(Long userId, YearMonth yearMonth) {

        List<ExpenseDto> topExpenses =
                expenseService.getTop5ByMonth(userId, yearMonth);

        if (topExpenses.isEmpty()) {
            return "No expenses found for " + yearMonth;
        }

        StringBuilder response = new StringBuilder("Top expenses:\n");

        int i = 1;
        for (ExpenseDto e : topExpenses) {
            response.append(i++)
                    .append(". ")
                    .append(e.getDescription())
                    .append(" - ₹")
                    .append(e.getAmount())
                    .append("\n");
        }

        return response.toString();
    }

    private String getCategorySummary(Long userId, YearMonth yearMonth) {

        List<ExpenseDto> expenses =
                expenseService.getExpenses(userId, yearMonth, null, null);

        Map<String, Integer> categoryMap = new HashMap<>();

        for (ExpenseDto e : expenses) {
            String category = e.getCategoryName() != null ? e.getCategoryName() : "Other";

            categoryMap.put(category,
                    categoryMap.getOrDefault(category, 0) + e.getAmount());
        }

        StringBuilder response = new StringBuilder("Category spending:\n");

        categoryMap.forEach((cat, amt) ->
                response.append(cat).append(": ₹").append(amt).append("\n"));

        return response.toString();
    }
}
