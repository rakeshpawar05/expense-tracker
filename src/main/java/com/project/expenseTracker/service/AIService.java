package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.AIInsightDto;
import com.project.expenseTracker.dto.AIRequest;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.IntentResult;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ExpenseService expenseService;
    private final MonthService monthService;
    private final AIIntentService aiIntentService;
    private final AIResponseService aiResponseService;

    public String processQuery(AIRequest request) {

        IntentResult intent = aiIntentService.detect(request.getMessage());

        final YearMonth yearMonth = parseYearMonthSafely(intent.getYearMonth());
        return switch (intent.getIntent()) {
            case TOTAL_EXPENSE -> getTotalExpense(request, yearMonth);
            case TOP_EXPENSES -> getTopExpenses(request, yearMonth);
            case CATEGORY_SUMMARY -> getCategorySummary(request, yearMonth, intent.getCategory());
            case EVENT_SUMMARY -> getEventSummary(request, intent.getEvent());
            default -> getGenericResponse(request);
        };
    }

    public AIInsightDto getMonthlyInsight(Long userId, YearMonth requestedYm) {
        LocalDate today = LocalDate.now();
        YearMonth currentSystemYm = YearMonth.from(today);
        int dayOfMonth = today.getDayOfMonth();

        YearMonth targetYm = requestedYm != null ? requestedYm : currentSystemYm;
        boolean isCurrentMonth = targetYm.equals(currentSystemYm);

        // Dynamic temporal rule: On or before 5th of current month, display previous month wrap-up
        boolean showPreviousMonth = isCurrentMonth && dayOfMonth <= 5;

        YearMonth analyzedYm = showPreviousMonth ? currentSystemYm.minusMonths(1) : targetYm;
        String periodType = showPreviousMonth ? "PREVIOUS_MONTH_WRAPUP"
                : (isCurrentMonth ? "CURRENT_MONTH_PACING" : "HISTORICAL_SUMMARY");

        Optional<Month> monthOpt = monthService.getMonthByUserIdAndYearMonth(userId, analyzedYm);
        if (monthOpt.isEmpty() && showPreviousMonth) {
            analyzedYm = targetYm;
            periodType = "CURRENT_MONTH_PACING";
            monthOpt = monthService.getMonthByUserIdAndYearMonth(userId, analyzedYm);
        }

        String title = formatInsightTitle(analyzedYm, periodType);

        if (monthOpt.isEmpty()) {
            return AIInsightDto.builder()
                    .title(title)
                    .periodType(periodType)
                    .analyzedYearMonth(analyzedYm)
                    .headline("No Financial Records Yet")
                    .insight("No income or expense records found for " + analyzedYm + ". Add transactions to unlock AI-powered budget pacing.")
                    .keyTakeaway("Log your monthly income and expenses to track cashflow.")
                    .statusTag("No Data")
                    .totalEarning(0.0)
                    .totalExpense(0.0)
                    .balance(0.0)
                    .spendPercentage(0.0)
                    .topCategory("None")
                    .topCategoryAmount(0.0)
                    .build();
        }

        Month month = monthOpt.get();
        List<Expense> expenses = month.getExpenses() != null ? month.getExpenses() : Collections.emptyList();
        Double totalEarning = month.getEarning() != null ? month.getEarning() : 0.0;
        Double totalExpense = expenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
        Double balance = totalEarning - totalExpense;
        Double spendPercentage = totalEarning > 0 ? (totalExpense / totalEarning) * 100.0 : 0.0;

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense e : expenses) {
            String catName = e.getCategory() != null ? e.getCategory().getName() : "General";
            categoryTotals.put(catName, categoryTotals.getOrDefault(catName, 0.0) + (e.getAmount() != null ? e.getAmount() : 0.0));
        }

        Map.Entry<String, Double> topCat = categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        String topCategory = topCat != null ? topCat.getKey() : "None";
        Double topCategoryAmount = topCat != null ? topCat.getValue() : 0.0;

        String defaultStatusTag;
        if (totalEarning > 0 && spendPercentage > 90) {
            defaultStatusTag = "Overspending Alert";
        } else if (totalEarning > 0 && spendPercentage > 75) {
            defaultStatusTag = "Caution: High Spend";
        } else if (totalEarning > 0 && spendPercentage <= 50) {
            defaultStatusTag = "Healthy Surplus";
        } else if (totalEarning > 0) {
            defaultStatusTag = "Balanced Pace";
        } else {
            defaultStatusTag = "Tracking Active";
        }

        return aiResponseService.generateMonthlyInsight(
                title, periodType, analyzedYm, dayOfMonth,
                totalEarning, totalExpense, balance, spendPercentage,
                expenses.size(), topCategory, topCategoryAmount, categoryTotals, defaultStatusTag
        );
    }

    private String formatInsightTitle(YearMonth ym, String periodType) {
        String mName = ym.getMonth().name().substring(0, 1).toUpperCase() + ym.getMonth().name().substring(1).toLowerCase();
        if ("PREVIOUS_MONTH_WRAPUP".equals(periodType)) {
            return mName + " " + ym.getYear() + " Wrap-Up";
        } else if ("CURRENT_MONTH_PACING".equals(periodType)) {
            return mName + " " + ym.getYear() + " Live Pacing";
        } else {
            return mName + " " + ym.getYear() + " Executive Summary";
        }
    }

    private String getTotalExpense(AIRequest aiRequest, YearMonth yearMonth) {
        List<ExpenseDto> expenses =
                expenseService.getExpenses(aiRequest.getUserId(), yearMonth, null, null, null);

        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);
    }

    private String getTopExpenses(AIRequest aiRequest, YearMonth yearMonth) {
        List<ExpenseDto> topExpenses =
                expenseService.getTop5ByMonth(aiRequest.getUserId(), yearMonth);

        if (topExpenses.isEmpty()) {
            return "No expenses found for " + yearMonth;
        }

        return aiResponseService.generateResponse(aiRequest.getMessage(), topExpenses);
    }

    private String getCategorySummary(AIRequest aiRequest, YearMonth yearMonth, String categoryName) {
        List<ExpenseDto> expenses =
                expenseService.getExpenses(aiRequest.getUserId(), yearMonth, categoryName, null, null);

        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);
    }

    private String getEventSummary(AIRequest aiRequest, String eventName) {
        List<ExpenseDto> expenses =  expenseService.getExpenses(aiRequest.getUserId(), null, null, null, eventName);

        if (expenses.isEmpty()) {
            return "No expenses found for event: " + eventName;
        }

        return aiResponseService.generateResponse(aiRequest.getMessage(), expenses);
    }

    private String getGenericResponse(AIRequest aiRequest) {
        return aiResponseService.generateResponse(aiRequest.getMessage(), List.of());
    }

    private YearMonth parseYearMonthSafely(String ymStr) {
        if (ymStr == null || ymStr.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(ymStr);
        } catch (Exception e) {
            return null;
        }
    }
}
