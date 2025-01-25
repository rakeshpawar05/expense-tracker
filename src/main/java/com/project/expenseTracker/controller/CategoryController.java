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
    public CategoryDto getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id);
    }

    @GetMapping
    public List<CategoryDto> getCategoriesByMonthId(@RequestParam Long monthId){
        return categoryService.getCategoryByMonthId(monthId);
    }

    @PostMapping
    public long createCategory(@RequestBody CategoryDto categoryDto){
        return categoryService.createCategory(categoryDto);
    }

    @PutMapping("/{id}")
    public CategoryDto updateCategory(@RequestBody CategoryDto categoryDto, @PathVariable Long id){
        return categoryService.updateCategory(categoryDto, id);
    }

    @GetMapping("/{id}/amount")
    public int getAmountById(@PathVariable Long id){
        return categoryService.getAmountById(id);
    }

    @DeleteMapping("/{id}")
    public long deleteCategory(@PathVariable Long id){
        return categoryService.deleteCategoryById(id);
    }
}
