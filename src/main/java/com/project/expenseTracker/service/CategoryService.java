package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.CategoryDto;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private MonthRepository monthRepository;
    private ExpenseRepository expenseRepository;

    public Long createCategory(CategoryDto categoryDto){
        User user = userRepository.findById(categoryDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found"));
        Month month = monthRepository.findByNameAndYear(categoryDto.getMonthName().split(",")[0],
                Integer.parseInt(categoryDto.getMonthName().split(",")[1])).orElseThrow(
                () -> new ResourceNotFoundException("Month not found"));

        Category category = mapDtoToEntity(null, categoryDto, user, month);
        return categoryRepository.save(category).getId();
    }

    public CategoryDto updateCategory(CategoryDto categoryDto, Long id){
        Category existingCategory = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Category not found")
        );
        Category category = categoryRepository.save(mapDtoToEntity(existingCategory, categoryDto, null, null));
        return mapEntityToDto(category);
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Category not found"));
        return mapEntityToDto(category);
    }

    public List<CategoryDto> getCategoryByMonthId(Long monthId){
        List<Category> categories = categoryRepository.findByMonthId(monthId);
        return categories.stream().map(CategoryService::mapEntityToDto).toList();
    }

    public int getAmountById(Long id) {
        List<Expense> expenses = expenseRepository.findByCategoryId(id);
        return  expenses.stream().mapToInt(Expense::getAmount).sum();
    }

    public Long deleteCategoryById(Long id){
        categoryRepository.deleteById(id);
        return id;
    }

    private Category mapDtoToEntity(Category existingCategory, CategoryDto categoryDto, User user, Month month){
        if(existingCategory != null){
            existingCategory.setName(categoryDto.getName());
            return existingCategory;
        }
        return Category.builder()
                .name(categoryDto.getName())
                .user(user)
                .month(month)
                .build();
    }

    public static CategoryDto mapEntityToDto(Category category){
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .monthName(category.getMonth().getName() + "," + category.getMonth().getYear())
                .userId(category.getUser().getId())
                .expenses(category.getExpenses().stream()
                        .filter(expense -> expense.getMonth() == category.getMonth())
                        .map(ExpenseService::mapEntityToDTo).toList())
                .build();
    }

}
