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
    public List<ExpenseDto> getExpensesByMonth(@RequestParam Long monthId){
        return expenseService.getExpenseByMonth(monthId);
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
}
