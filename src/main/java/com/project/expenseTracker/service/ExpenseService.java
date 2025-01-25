package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.entity.Expense;
import com.project.expenseTracker.entity.Month;
import com.project.expenseTracker.entity.User;
import com.project.expenseTracker.exception.ResourceNotFoundException;
import com.project.expenseTracker.repository.CategoryRepository;
import com.project.expenseTracker.repository.ExpenseRepository;
import com.project.expenseTracker.repository.MonthRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final MonthRepository monthRepository;
    private final CategoryRepository categoryRepository;

    public long createExpense(ExpenseDto expenseDto){
        Category category = categoryRepository.findById(expenseDto.getCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("Category not found"));
        Month month = monthRepository.findById(expenseDto.getMonthId()).orElseThrow(
                () -> new ResourceNotFoundException("Month not found"));

        Expense expense = mapDtoToEntity(null, expenseDto, month, category);
        return expenseRepository.save(expense).getId();
    }

    public ExpenseDto updateExpense(ExpenseDto expenseDto, Long expenseId){
        Expense existingExpense = expenseRepository.findById(expenseId).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
        Expense expense = expenseRepository.save(mapDtoToEntity(existingExpense, expenseDto, null, null));
        return mapEntityToDTo(expense);
    }

    public ExpenseDto getExpenseById(Long id){
        Expense expense = expenseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
        return mapEntityToDTo(expense);
    }

    public List<ExpenseDto> getExpenseByMonth(Long monthId){
        List<Expense> expenses = expenseRepository.findByMonthId(monthId);
        return expenses.stream().map(ExpenseService::mapEntityToDTo).toList();
    }

    public List<ExpenseDto> getExpenseByCategory(Long categoryId){
        List<Expense> expenses = expenseRepository.findByCategoryId(categoryId);
        return expenses.stream().map(ExpenseService::mapEntityToDTo).toList();
    }

    public long deleteExpenseById(Long id){
        expenseRepository.deleteById(id);
        return id;
    }

    private Expense mapDtoToEntity(Expense existingExpense, ExpenseDto expenseDto, Month month, Category category){
        if(existingExpense != null){
            existingExpense.setDescription(expenseDto.getDescription());
            existingExpense.setAmount(expenseDto.getAmount());
        }
        return Expense.builder()
                .amount(expenseDto.getAmount())
                .description(expenseDto.getDescription())
                .month(month)
                .category(category)
                .build();
    }

    public static ExpenseDto mapEntityToDTo(Expense expense){
        return ExpenseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .monthId(expense.getMonth().getId())
                .categoryId(expense.getCategory().getId())
                .build();
    }
}
