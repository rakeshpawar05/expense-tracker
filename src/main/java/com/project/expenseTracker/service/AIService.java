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

    private final AIResponseService aiResponseService;

    public String processQuery(AIRequest request) {

        IntentResult intent = aiIntentService.detect(request.getMessage());

        final YearMonth yearMonth = intent.getYearMonth() != null ? YearMonth.parse(intent.getYearMonth()): null;
        return switch (intent.getIntent()) {
            case TOTAL_EXPENSE -> getTotalExpense(request, yearMonth);
            case TOP_EXPENSES -> getTopExpenses(request, yearMonth);
            case CATEGORY_SUMMARY -> getCategorySummary(request, yearMonth, intent.getCategory());
            case EVENT_SUMMARY -> getEventSummary(request, intent.getEvent());
            default -> getGenericResponse(request);
        };
    }

    private String getTotalExpense(AIRequest aiRequest, YearMonth yearMonth) {

        List<ExpenseDto> expenses =
                expenseService.getExpenses(aiRequest.getUserId(), yearMonth, null, null, null);

        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);
//        int total = expenses.stream()
//                .mapToInt(ExpenseDto::getAmount)
//                .sum();
//
//        return "You spent ₹" + total + " in " + yearMonth;
    }

    private String getTopExpenses(AIRequest aiRequest, YearMonth yearMonth) {

        List<ExpenseDto> topExpenses =
                expenseService.getTop5ByMonth(aiRequest.getUserId(), yearMonth);

        if (topExpenses.isEmpty()) {
            return "No expenses found for " + yearMonth;
        }

        return aiResponseService.generateResponse(aiRequest.getMessage(), topExpenses);

//        StringBuilder response = new StringBuilder("Top expenses:\n");
//
//        int i = 1;
//        for (ExpenseDto e : topExpenses) {
//            response.append(i++)
//                    .append(". ")
//                    .append(e.getDescription())
//                    .append(" - ₹")
//                    .append(e.getAmount())
//                    .append("\n");
//        }
//
//        return response.toString();
    }

    private String getCategorySummary(AIRequest aiRequest, YearMonth yearMonth, String categoryName) {

        List<ExpenseDto> expenses =
                expenseService.getExpenses(aiRequest.getUserId(), yearMonth, categoryName, null, null);


        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);
//        Map<String, Integer> categoryMap = new HashMap<>();
//
//        for (ExpenseDto e : expenses) {
//            String category = e.getCategoryName() != null ? e.getCategoryName() : "Other";
//
//            categoryMap.put(category,
//                    categoryMap.getOrDefault(category, 0) + e.getAmount());
//        }
//
//        StringBuilder response = new StringBuilder("Category spending:\n");
//
//        categoryMap.forEach((cat, amt) ->
//                response.append(cat).append(": ₹").append(amt).append("\n"));
//
//        return response.toString();
    }

    private String getEventSummary(AIRequest aiRequest, String eventName) {
        List<ExpenseDto> expenses =  expenseService.getExpenses(aiRequest.getUserId(), null, null, null, eventName);

        if (expenses.isEmpty()) {
            return "No expenses found for event: " + eventName;
        }

        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);

//        int total = expenses.stream()
//                .mapToInt(ExpenseDto::getAmount)
//                .sum();
//
//        return "You spent ₹" + total + " on " + eventName;
    }

    private String getGenericResponse(AIRequest aiRequest) {

        return aiResponseService.generateResponse(aiRequest.getMessage(), List.of());
    }
}
