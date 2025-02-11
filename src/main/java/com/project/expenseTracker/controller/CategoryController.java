package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.CategoryDto;
import com.project.expenseTracker.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {


    private CategoryService categoryService;

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable("id") Long id){
        return categoryService.getCategoryById(id);
    }

    @GetMapping
    public List<CategoryDto> getCategoriesByMonthName(@RequestParam("userId") Long userId, @RequestParam("monthName") String monthName){
        return categoryService.getCategoryByMonthId(userId, monthName);
    }

    @PostMapping
    public long createCategory(@RequestBody CategoryDto categoryDto){
        return categoryService.createCategory(categoryDto);
    }

    @PutMapping("/{id}")
    public CategoryDto updateCategory(@RequestBody CategoryDto categoryDto, @PathVariable("id") Long id){
        return categoryService.updateCategory(categoryDto, id);
    }

    @GetMapping("/{id}/amount")
    public int getAmountById(@PathVariable("id") Long id){
        return categoryService.getAmountById(id);
    }

    @DeleteMapping("/{id}")
    public long deleteCategory(@PathVariable("id") Long id){
        return categoryService.deleteCategoryById(id);
    }
}
