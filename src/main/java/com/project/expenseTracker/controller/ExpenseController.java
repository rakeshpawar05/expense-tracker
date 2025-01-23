package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.service.ExpenseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private ExpenseService ExpenseService;

    @GetMapping("/{id}")
    public ExpenseDto getExpenseById(Long id){
        return new ExpenseDto();
    }

    @GetMapping
    public List<ExpenseDto> getExpenses(){
        return List.of();
    }

    @PostMapping
    public void createExpense(@RequestBody ExpenseDto ExpenseDto){

    }

    @PutMapping("/{id}")
    public void updateExpense(@RequestBody ExpenseDto ExpenseDto, Long id){

    }

    @DeleteMapping("/{id}")
    public void deleteExpense(Long id){

    }
}
