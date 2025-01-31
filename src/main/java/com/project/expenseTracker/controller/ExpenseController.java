package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.ExpenseDto;
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
    public ExpenseDto getExpenseById(@PathVariable Long id){
        return expenseService.getExpenseById(id);
    }

    @GetMapping
    public List<ExpenseDto> getExpenses(@RequestParam Long userId,
                                        @RequestParam(required = false) String monthName,
                                        @RequestParam(required = false) String categoryName,
                                        @RequestParam(required = false) String expenseName){
        return expenseService.getExpenses(userId, monthName, categoryName, expenseName);
    }

    @PostMapping
    public long createExpense(@RequestBody ExpenseDto expenseDto){
        return expenseService.createExpense(expenseDto);
    }

    @PutMapping("/{id}")
    public ExpenseDto updateExpense(@RequestBody ExpenseDto expenseDto, @PathVariable Long id){
        return expenseService.updateExpense(expenseDto, id);
    }

    @DeleteMapping("/{id}")
    public long deleteExpense(@PathVariable Long id){
        return expenseService.deleteExpenseById(id);
    }

    @GetMapping("/top5")
    public List<ExpenseDto> getTop5ByMonth(@RequestParam Long userId, @RequestParam String monthName) {
        return expenseService.getTop5ByMonth(userId, monthName);
    }
}
