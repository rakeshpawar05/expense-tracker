package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.CategoryDto;
import com.project.expenseTracker.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {


    private CategoryService categoryService;

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(Long id){
        return new CategoryDto();
    }

    @GetMapping
    public List<CategoryDto> getCategories(){
        return List.of();
    }

    @PostMapping
    public long createCategory(@RequestBody CategoryDto categoryDto, HttpServletRequest httpServletRequest){
//        httpServletRequest.s
        return categoryService.createCategory(categoryDto);
    }

    @PutMapping("/{id}")
    public void updateCategory(@RequestBody CategoryDto categoryDto, Long id){

    }

    @DeleteMapping("/{id}")
    public void deleteCategory(Long id){

    }
}
