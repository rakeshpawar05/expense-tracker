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
import com.project.expenseTracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.project.expenseTracker.service.MonthService.getMonthName;
import static com.project.expenseTracker.service.MonthService.getMonthYear;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final MonthRepository monthRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MonthService monthService;

    public long createExpense(ExpenseDto expenseDto){

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentPrincipalName = authentication.getName();
//        System.out.println("user " + currentPrincipalName);

        User user = userRepository.findById(expenseDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

//        String monthName = expenseDto.getMonthName().split(",")[0];
//        int year = Integer.parseInt(expenseDto.getMonthName().split(",")[1]);
        Month month = monthRepository.findByNameAndYearAndUserId(getMonthName(expenseDto.getMonthName())
                ,getMonthYear(expenseDto.getMonthName()), user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Month not found")
        );
//        if(month.isEmpty()){
//            Long createdMonthId = monthService.createMonth(MonthDto.builder()
//                    .name(expenseDto.getMonthName())
//                    .userId(user.getId())
//                    .build());
//            month = monthRepository.findById(createdMonthId);
//        }

        Optional<Category> category = categoryRepository.findByNameAndMonthIdAndUserId(expenseDto.getCategoryName(), month.getId(), user.getId());

        Expense expense = mapDtoToEntity(null, expenseDto, month, category.orElse(null), user);
        return expenseRepository.save(expense).getId();
    }

    public ExpenseDto updateExpense(ExpenseDto expenseDto, Long expenseId){
        Expense existingExpense = expenseRepository.findById(expenseId).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
//        Month month = existingExpense.getMonth();
        Expense expense = expenseRepository.save(mapDtoToEntity(existingExpense, expenseDto, null, null, null));
        return mapEntityToDTo(expense);
    }

    public ExpenseDto getExpenseById(Long id){
        Expense expense = expenseRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Expense not found"));
        return mapEntityToDTo(expense);
    }

    public List<ExpenseDto> getExpenses(Long userId, String monthName, String categoryName, String expenseName){
//        String name = null;
//        Integer year = null;
//        if(monthName != null){
//            name = monthName.split(",")[0];
//            year = Integer.parseInt(monthName.split(",")[1]);
//        }

        List<Expense> expenses = expenseRepository.findByFilters(userId, getMonthName(monthName), getMonthYear(monthName), categoryName, expenseName);
        return expenses.stream().map(ExpenseService::mapEntityToDTo).toList();
    }

//    public List<ExpenseDto> getExpenseByCategory(Long categoryId){
//        List<Expense> expenses = expenseRepository.findByCategoryId(categoryId);
//        return expenses.stream().map(ExpenseService::mapEntityToDTo).toList();
//    }

    public long deleteExpenseById(Long id){
        expenseRepository.deleteById(id);
        return id;
    }

    private Expense mapDtoToEntity(Expense existingExpense, ExpenseDto expenseDto, Month month, Category category, User user) {
        if(existingExpense != null){
            existingExpense.setDescription(expenseDto.getDescription());
            existingExpense.setAmount(expenseDto.getAmount());
            existingExpense.setDate(expenseDto.getDate());
            return existingExpense;
        }
        return Expense.builder()
                .amount(expenseDto.getAmount())
                .description(expenseDto.getDescription())
                .date(expenseDto.getDate())
                .month(month)
                .category(category)
                .user(user)
                .build();
    }

    public static ExpenseDto mapEntityToDTo(Expense expense){
        return ExpenseDto.builder()
                .id(expense.getId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .date(expense.getDate())
                .monthName(expense.getMonth().getName() +","+ expense.getMonth().getYear())
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .userId(expense.getUser().getId())
                .build();
    }

    public List<ExpenseDto> getTop5ByMonth(Long userId, String monthName){
        Month month = monthRepository.findByNameAndYearAndUserId(getMonthName(monthName),
                getMonthYear(monthName), userId).orElseThrow(
                () -> new ResourceNotFoundException("Month not found"));

        return month.getExpenses().stream().sorted(Comparator.comparing(Expense::getAmount).reversed()).limit(5).
    map(ExpenseService::mapEntityToDTo).toList();
    }
}
