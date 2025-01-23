package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.MonthDto;
import com.project.expenseTracker.service.MonthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/months")
@AllArgsConstructor
public class MonthController {

    private MonthService monthService;

    @GetMapping("/{id}")
    public MonthDto getMonthById(Long id){
        return new MonthDto();
    }

    @GetMapping
    public List<MonthDto> getMonths(){
        return List.of();
    }

    @PostMapping
    public long createMonth(@RequestBody MonthDto monthDto){
        return monthService.createMonth(monthDto);
    }

    @PutMapping("/{id}")
    public void updateMonth(@RequestBody MonthDto monthDto, Long id){

    }

    @DeleteMapping("/{id}")
    public void deleteMonth(Long id){

    }
}
