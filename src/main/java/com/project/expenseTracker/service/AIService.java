package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.AICategoryInsightDto;
import com.project.expenseTracker.dto.AIEventInsightDto;
import com.project.expenseTracker.dto.AIInsightDto;
import com.project.expenseTracker.dto.AIMonthDeltaInsightDto;
import com.project.expenseTracker.dto.AIParseExpenseRequest;
import com.project.expenseTracker.dto.AIRequest;
import com.project.expenseTracker.dto.CategoryMoMDeltaDto;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.IntentResult;
import com.project.expenseTracker.dto.ParsedExpenseDto;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.entity.Event;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.repository.CategoryRepository;
import com.project.expenseTracker.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AIService {

    private final ExpenseService expenseService;
    private final MonthService monthService;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final AIIntentService aiIntentService;
    private final AIResponseService aiResponseService;

    public ParsedExpenseDto parseExpense(AIParseExpenseRequest request) {
        String effectiveDate = request.getDefaultDate() != null && !request.getDefaultDate().isBlank()
                ? request.getDefaultDate()
                : LocalDate.now().toString();

        List<String> availableCategories = Collections.emptyList();
        List<String> availableEvents = Collections.emptyList();

        if (request.getUserId() != null) {
            try {
                YearMonth ym = request.getYearMonth() != null ? request.getYearMonth() : YearMonth.from(LocalDate.parse(effectiveDate));
                Optional<Month> mOpt = monthService.getMonthByUserIdAndYearMonth(request.getUserId(), ym);
                if (mOpt.isPresent() && mOpt.get().getCategories() != null) {
                    availableCategories = mOpt.get().getCategories().stream()
                            .map(Category::getName)
                            .toList();
                }

                List<Event> userEvents = eventRepository.findByUserId(request.getUserId());
                if (userEvents != null) {
                    availableEvents = userEvents.stream()
                            .map(Event::getName)
                            .toList();
                }
            } catch (Exception ignored) {
            }
        }

        return aiResponseService.parseExpenseText(
                request.getText(),
                effectiveDate,
                availableCategories,
                availableEvents
        );
    }

    public AICategoryInsightDto getCategoryInsight(Long userId, Long categoryId, String categoryName, YearMonth requestedYm) {
        LocalDate today = LocalDate.now();
        YearMonth targetYm = requestedYm != null ? requestedYm : YearMonth.from(today);

        Category targetCat = null;
        if (categoryId != null) {
            targetCat = categoryRepository.findById(categoryId).orElse(null);
        }

        if (targetCat == null && categoryName != null && !categoryName.isBlank()) {
            List<Category> userCats = categoryRepository.findByUserIdAndMonthYearNumAndMonthMonthNum(
                    userId, targetYm.getYear(), targetYm.getMonthValue());
            targetCat = userCats.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(categoryName.trim()))
                    .findFirst()
                    .orElse(null);
        }

        final Category resolvedCat = targetCat;
        final String finalCatName = resolvedCat != null ? resolvedCat.getName() : (categoryName != null ? categoryName : "General");

        List<Expense> catExpenses = Collections.emptyList();
        Month currentMonth = null;
        if (resolvedCat != null) {
            currentMonth = resolvedCat.getMonth();
            final Month catMonth = resolvedCat.getMonth();
            catExpenses = resolvedCat.getExpenses() != null
                    ? resolvedCat.getExpenses().stream()
                            .filter(e -> e.getMonth() == catMonth)
                            .toList()
                    : Collections.emptyList();
        } else {
            Optional<Month> mOpt = monthService.getMonthByUserIdAndYearMonth(userId, targetYm);
            if (mOpt.isPresent()) {
                currentMonth = mOpt.get();
                catExpenses = currentMonth.getExpenses() != null
                        ? currentMonth.getExpenses().stream()
                                .filter(e -> e.getCategory() != null && e.getCategory().getName().equalsIgnoreCase(finalCatName))
                                .toList()
                        : Collections.emptyList();
            }
        }

        Double currentSpent = catExpenses.stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0).sum();
        int count = catExpenses.size();
        Double avgPerTx = count > 0 ? currentSpent / count : 0.0;

        Expense largest = catExpenses.stream()
                .max((a, b) -> Integer.compare(a.getAmount() != null ? a.getAmount() : 0, b.getAmount() != null ? b.getAmount() : 0))
                .orElse(null);
        String largestDesc = largest != null ? largest.getDescription() : "None";
        Double largestAmount = largest != null && largest.getAmount() != null ? largest.getAmount().doubleValue() : 0.0;

        Double monthIncome = currentMonth != null && currentMonth.getEarning() != null ? currentMonth.getEarning() : 0.0;
        Double totalMonthExpenses = currentMonth != null && currentMonth.getExpenses() != null
                ? currentMonth.getExpenses().stream().mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0).sum()
                : currentSpent;

        Double percentOfExpenses = totalMonthExpenses > 0 ? (currentSpent / totalMonthExpenses) * 100.0 : 0.0;
        Double percentOfIncome = monthIncome > 0 ? (currentSpent / monthIncome) * 100.0 : 0.0;

        // Previous month comparison
        YearMonth prevYm = targetYm.minusMonths(1);
        List<Category> prevCats = categoryRepository.findByUserIdAndMonthYearNumAndMonthMonthNum(
                userId, prevYm.getYear(), prevYm.getMonthValue());
        final Category prevCat = prevCats.stream()
                .filter(c -> c.getName().equalsIgnoreCase(finalCatName))
                .findFirst()
                .orElse(null);

        Double prevSpent = 0.0;
        Double pctChange = null;
        if (prevCat != null && prevCat.getExpenses() != null) {
            final Month prevMonth = prevCat.getMonth();
            prevSpent = prevCat.getExpenses().stream()
                    .filter(e -> e.getMonth() == prevMonth)
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    .sum();
            if (prevSpent > 0) {
                pctChange = ((currentSpent - prevSpent) / prevSpent) * 100.0;
            }
        }

        // Recommended budget ceiling
        Double recommendedCeiling;
        if (monthIncome > 0) {
            boolean isDiscretionary = finalCatName.toLowerCase().matches(".*(entertainment|movie|dining|restaurant|shopping|vacation|travel|hobby).*");
            recommendedCeiling = isDiscretionary
                    ? (double) Math.round(monthIncome * 0.15)
                    : (double) Math.round(monthIncome * 0.25);
        } else {
            recommendedCeiling = currentSpent > 0 ? (double) Math.round(currentSpent * 1.1) : 5000.0;
        }

        // Health status default
        String defaultHealth;
        if (percentOfExpenses > 40 || (pctChange != null && pctChange > 50)) {
            defaultHealth = "ELEVATED";
        } else if (percentOfExpenses > 55) {
            defaultHealth = "CRITICAL";
        } else if (percentOfExpenses < 15) {
            defaultHealth = "HEALTHY";
        } else {
            defaultHealth = "BALANCED";
        }

        return aiResponseService.generateCategoryInsight(
                finalCatName, targetYm, currentSpent, count, avgPerTx,
                percentOfExpenses, percentOfIncome, prevSpent, pctChange,
                recommendedCeiling, largestDesc, largestAmount, defaultHealth
        );
    }

    public AIEventInsightDto getEventInsight(Long userId, Long eventId, String eventName) {
        Event targetEvent = null;
        if (eventId != null) {
            targetEvent = eventRepository.findById(eventId).orElse(null);
        }

        if (targetEvent == null && eventName != null && !eventName.isBlank()) {
            List<Event> userEvents = eventRepository.findByUserId(userId);
            if (userEvents != null) {
                targetEvent = userEvents.stream()
                        .filter(ev -> ev.getName().equalsIgnoreCase(eventName.trim()))
                        .findFirst()
                        .orElse(null);
            }
        }

        final Event event = targetEvent;
        String finalEventName = event != null ? event.getName() : (eventName != null ? eventName : "Event");

        List<Expense> expenses = (event != null && event.getExpenses() != null)
                ? event.getExpenses()
                : Collections.emptyList();

        Double totalSpent = expenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
        int count = expenses.size();

        // Calculate active days & timespan
        List<String> dates = expenses.stream()
                .map(Expense::getDate)
                .filter(d -> d != null && !d.isBlank())
                .sorted()
                .toList();

        String startDate = dates.isEmpty() ? null : dates.get(0);
        String endDate = dates.isEmpty() ? null : dates.get(dates.size() - 1);
        int activeDays = 1;
        if (startDate != null && endDate != null) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                activeDays = (int) Math.max(1, days);
            } catch (Exception ignored) {
            }
        }

        Double dailyBurnRate = activeDays > 0 ? (double) Math.round(totalSpent / activeDays) : totalSpent;

        // Largest expense
        Expense largest = expenses.stream()
                .max((a, b) -> Integer.compare(a.getAmount() != null ? a.getAmount() : 0, b.getAmount() != null ? b.getAmount() : 0))
                .orElse(null);
        String largestDesc = largest != null ? largest.getDescription() : "None";
        Double largestAmount = largest != null && largest.getAmount() != null ? largest.getAmount().doubleValue() : 0.0;

        // Category breakdown
        Map<String, Double> categoryBreakdown = new HashMap<>();
        for (Expense e : expenses) {
            String catName = e.getCategory() != null ? e.getCategory().getName() : "General";
            categoryBreakdown.put(catName, categoryBreakdown.getOrDefault(catName, 0.0) + (e.getAmount() != null ? e.getAmount() : 0.0));
        }

        Map.Entry<String, Double> topCat = categoryBreakdown.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        String topCategory = topCat != null ? topCat.getKey() : "General";
        Double topCategoryAmount = topCat != null ? topCat.getValue() : 0.0;

        // Default verdict tag
        String defaultVerdict;
        if (dailyBurnRate > 10000) {
            defaultVerdict = "Premium Outflow";
        } else if (dailyBurnRate > 4000) {
            defaultVerdict = "Moderate Pacing";
        } else if (topCategory.toLowerCase().contains("travel") || topCategory.toLowerCase().contains("flight")) {
            defaultVerdict = "Travel Heavy";
        } else {
            defaultVerdict = "Budget Friendly";
        }

        return aiResponseService.generateEventInsight(
                event != null ? event.getId() : eventId,
                finalEventName,
                totalSpent,
                count,
                activeDays,
                dailyBurnRate,
                startDate,
                endDate,
                largestDesc,
                largestAmount,
                topCategory,
                topCategoryAmount,
                categoryBreakdown,
                defaultVerdict
        );
    }

    public AIMonthDeltaInsightDto getMonthDeltaInsight(Long userId, YearMonth requestedCurrentYm, YearMonth requestedCompareYm) {
        YearMonth currentYm = requestedCurrentYm != null ? requestedCurrentYm : YearMonth.now();
        YearMonth previousYm = requestedCompareYm != null ? requestedCompareYm : currentYm.minusMonths(1);

        Optional<Month> currentMonthOpt = monthService.getMonthByUserIdAndYearMonth(userId, currentYm);
        Optional<Month> previousMonthOpt = monthService.getMonthByUserIdAndYearMonth(userId, previousYm);

        List<Expense> currentExpenses = currentMonthOpt.map(Month::getExpenses).orElse(Collections.emptyList());
        List<Expense> previousExpenses = previousMonthOpt.map(Month::getExpenses).orElse(Collections.emptyList());

        Double currentSpent = currentExpenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
        Double previousSpent = previousExpenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();

        Double absoluteChange = currentSpent - previousSpent;
        Double percentageChange = previousSpent > 0
                ? ((currentSpent - previousSpent) / previousSpent) * 100.0
                : (currentSpent > 0 ? 100.0 : 0.0);

        Double currentEarning = currentMonthOpt.map(Month::getEarning).orElse(0.0);
        Double previousEarning = previousMonthOpt.map(Month::getEarning).orElse(0.0);

        Double currentSavingsRate = currentEarning > 0
                ? Math.max(-100.0, ((currentEarning - currentSpent) / currentEarning) * 100.0)
                : 0.0;
        Double previousSavingsRate = previousEarning > 0
                ? Math.max(-100.0, ((previousEarning - previousSpent) / previousEarning) * 100.0)
                : 0.0;

        int currentTxCount = currentExpenses.size();
        int previousTxCount = previousExpenses.size();

        // Category breakdown for current and previous
        Map<String, Double> currentCats = new HashMap<>();
        for (Expense e : currentExpenses) {
            String name = e.getCategory() != null ? e.getCategory().getName() : "General";
            currentCats.put(name, currentCats.getOrDefault(name, 0.0) + (e.getAmount() != null ? e.getAmount() : 0.0));
        }

        Map<String, Double> previousCats = new HashMap<>();
        for (Expense e : previousExpenses) {
            String name = e.getCategory() != null ? e.getCategory().getName() : "General";
            previousCats.put(name, previousCats.getOrDefault(name, 0.0) + (e.getAmount() != null ? e.getAmount() : 0.0));
        }

        Set<String> allCategoryNames = new HashSet<>();
        allCategoryNames.addAll(currentCats.keySet());
        allCategoryNames.addAll(previousCats.keySet());

        List<CategoryMoMDeltaDto> categoryDeltas = new ArrayList<>();
        for (String catName : allCategoryNames) {
            Double cur = currentCats.getOrDefault(catName, 0.0);
            Double prev = previousCats.getOrDefault(catName, 0.0);
            Double delta = cur - prev;
            Double pct = prev > 0 ? ((cur - prev) / prev) * 100.0 : (cur > 0 ? 100.0 : 0.0);

            categoryDeltas.add(CategoryMoMDeltaDto.builder()
                    .categoryName(catName)
                    .currentAmount((double) Math.round(cur))
                    .previousAmount((double) Math.round(prev))
                    .absoluteChange((double) Math.round(delta))
                    .percentageChange((double) Math.round(pct * 10.0) / 10.0)
                    .build());
        }

        // Sort by absolute change descending
        categoryDeltas.sort((a, b) -> Double.compare(b.getAbsoluteChange(), a.getAbsoluteChange()));

        String topSurgeCategory = null;
        Double topSurgeAmount = 0.0;
        Double topSurgePercentage = 0.0;

        String topCoolingCategory = null;
        Double topCoolingAmount = 0.0;
        Double topCoolingPercentage = 0.0;

        if (!categoryDeltas.isEmpty()) {
            CategoryMoMDeltaDto highest = categoryDeltas.get(0);
            if (highest.getAbsoluteChange() > 0) {
                topSurgeCategory = highest.getCategoryName();
                topSurgeAmount = highest.getAbsoluteChange();
                topSurgePercentage = highest.getPercentageChange();
            }

            CategoryMoMDeltaDto lowest = categoryDeltas.get(categoryDeltas.size() - 1);
            if (lowest.getAbsoluteChange() < 0) {
                topCoolingCategory = lowest.getCategoryName();
                topCoolingAmount = lowest.getAbsoluteChange();
                topCoolingPercentage = lowest.getPercentageChange();
            }
        }

        String defaultVerdict;
        if (percentageChange <= -12.0) {
            defaultVerdict = "Disciplined Contraction";
        } else if (percentageChange >= 20.0) {
            defaultVerdict = "High Outflow Surge";
        } else if (percentageChange >= 5.0) {
            defaultVerdict = "Moderate Creep";
        } else if (percentageChange <= -5.0) {
            defaultVerdict = "Frugal Savings";
        } else {
            defaultVerdict = "Balanced Flow";
        }

        return aiResponseService.generateMonthDeltaInsight(
                currentYm.toString(),
                previousYm.toString(),
                (double) Math.round(currentSpent),
                (double) Math.round(previousSpent),
                (double) Math.round(absoluteChange),
                (double) Math.round(percentageChange * 10.0) / 10.0,
                (double) Math.round(currentEarning),
                (double) Math.round(previousEarning),
                (double) Math.round(currentSavingsRate * 10.0) / 10.0,
                (double) Math.round(previousSavingsRate * 10.0) / 10.0,
                currentTxCount,
                previousTxCount,
                topSurgeCategory,
                topSurgeAmount,
                topSurgePercentage,
                topCoolingCategory,
                topCoolingAmount,
                topCoolingPercentage,
                categoryDeltas,
                defaultVerdict
        );
    }

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
