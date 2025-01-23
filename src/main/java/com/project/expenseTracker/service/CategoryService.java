package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.CategoryDto;
import com.project.expenseTracker.entity.Category;
import com.project.expenseTracker.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;

    public Long createCategory(CategoryDto categoryDto){
        Category save = categoryRepository.save(modelMapper.map(categoryDto, Category.class));
        return save.getId();
    }
}
