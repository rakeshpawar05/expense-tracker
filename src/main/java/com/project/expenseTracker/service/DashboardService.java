package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.CategorySummaryDto;
import com.project.expenseTracker.dto.DashboardDto;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.MonthRepository;
import com.project.expenseTracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MonthService monthService;
    private final ExpenseService expenseService;

    /**
     * Get complete dashboard data for a user in a specific month
     * Returns: month summary, total earnings, total expenses, balance, top 5 expenses, and category breakdown
     */
    public DashboardDto getDashboardData(Long userId, YearMonth yearMonth) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        Month month = monthService.getMonthByUserIdAndYearMonth(userId, yearMonth);

        // Get all expenses for the month
        List<Expense> monthExpenses = month.getExpenses() != null ? month.getExpenses() : new ArrayList<>();
        
        // Calculate totals
        Double totalEarning = month.getEarning() != null ? month.getEarning() : 0.0;
        Double totalExpense = monthExpenses.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();
        Double balance = totalEarning - totalExpense;

        // Get top 5 expenses
        List<ExpenseDto> topExpenses = monthExpenses.stream()
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(5)
                .map(ExpenseService::mapEntityToDTo)
                .toList();

        // Build category breakdown
        List<CategorySummaryDto> categoryBreakdown = buildCategoryBreakdown(monthExpenses);

        return DashboardDto.builder()
                .monthName(month.getName())
                .monthYear(month.getYear())
                .yearMonth(YearMonth.of(month.getYearNum(), month.getMonthNum()))
                .totalEarning(totalEarning)
                .totalExpense(totalExpense)
                .balance(balance)
                .topExpenses(topExpenses)
                .categoryBreakdown(categoryBreakdown)
                .totalTransactions(monthExpenses.size())
                .build();
    }

    /**
     * Build category-wise breakdown of expenses
     */
    private List<CategorySummaryDto> buildCategoryBreakdown(List<Expense> expenses) {
        return expenses.stream()
                .filter(expense -> expense.getCategory() != null)
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                categoryExpenses -> {
                                    Double totalAmount = categoryExpenses.stream()
                                            .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                                            .sum();
                                    return CategorySummaryDto.builder()
                                            .categoryName(categoryExpenses.get(0).getCategory().getName())
                                            .totalAmount(totalAmount)
                                            .expenseCount(categoryExpenses.size())
                                            .build();
                                }
                        )
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(CategorySummaryDto::getTotalAmount).reversed())
                .toList();
    }
}

