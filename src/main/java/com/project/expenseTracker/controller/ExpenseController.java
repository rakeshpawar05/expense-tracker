package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.ExpenseFeedResponseDto;
import com.project.expenseTracker.service.ExpenseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@AllArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/{id}")
    public ExpenseDto getExpenseById(@PathVariable("id") Long id){
        return expenseService.getExpenseById(id);
    }

    @GetMapping
    public List<ExpenseDto> getExpenses(@RequestParam("userId") Long userId,
                                        @RequestParam(name = "monthName", required = false) String monthName,
                                        @RequestParam(name = "categoryName", required = false) String categoryName,
                                        @RequestParam(name = "expenseName", required = false) String expenseName){
        return expenseService.getExpenses(userId, monthName, categoryName, expenseName);
    }

    @PostMapping
    public long createExpense(@RequestBody ExpenseDto expenseDto){
        return expenseService.createExpense(expenseDto);
    }

    @PutMapping("/{id}")
    public ExpenseDto updateExpense(@RequestBody ExpenseDto expenseDto, @PathVariable("id") Long id){
        return expenseService.updateExpense(expenseDto, id);
    }

    @DeleteMapping("/{id}")
    public long deleteExpense(@PathVariable("id") Long id){
        return expenseService.deleteExpenseById(id);
    }

    @GetMapping("/top5")
    public List<ExpenseDto> getTop5ByMonth(@RequestParam("userId") Long userId, @RequestParam("monthName") String monthName) {
        return expenseService.getTop5ByMonth(userId, monthName);
    }

    @GetMapping("/feed")
    public List<ExpenseDto> getExpenseFeed(@RequestParam("userId") Long userId,
                                           @RequestParam(name = "monthName", required = false) String monthName,
                                           @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit) {
        return expenseService.getExpenseFeed(userId, monthName, limit);
    }

    /**
     * Get paginated expense feed with cursor-based pagination for infinite scrolling
     * 
     * @param userId User ID (required)
     * @param monthName Optional month filter in format "MonthName,year" (e.g., "March,2026")
     * @param cursor Optional cursor for pagination - date of last item from previous page
     * @param limit Number of items per page (default 20)
     * @param fromDate Optional start date filter
     * @param toDate Optional end date filter
     * @param categoryId Optional category filter
     * @param eventId Optional event filter
     * @return ExpenseFeedResponseDto with items, nextCursor, and hasMore flag
     * 
     * Example: /api/expenses/feed/paginated?userId=1&monthName=March,2026&limit=20&cursor=2026-03-19T20:10:00
     */
    @GetMapping("/feed/paginated")
    public ExpenseFeedResponseDto getExpenseFeedPaginated(
            @RequestParam("userId") Long userId,
            @RequestParam(name = "monthName", required = false) String monthName,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit,
            @RequestParam(name = "fromDate", required = false) String fromDate,
            @RequestParam(name = "toDate", required = false) String toDate,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "eventId", required = false) Long eventId) {
        return expenseService.getExpenseFeedWithCursor(userId, monthName, cursor, limit, fromDate, toDate, categoryId, eventId);
    }
}
