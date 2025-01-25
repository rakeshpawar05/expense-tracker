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
    public MonthDto getMonthById(@PathVariable Long id){
        return monthService.getMonthById(id);
    }

    @GetMapping
    public List<MonthDto> getMonths(@RequestParam Long userId){
        return monthService.getMonthByUserId(userId);
    }

    @PostMapping
    public long createMonth(@RequestBody MonthDto monthDto){
        return monthService.createMonth(monthDto);
    }

    @PutMapping("/{id}")
    public MonthDto updateMonth(@RequestBody MonthDto monthDto, @PathVariable Long id){
        return monthService.updateMonth(id, monthDto);
    }

    @GetMapping("/{id}/amount")
    public int getAmountForMonth(@PathVariable Long id){
        return monthService.getAmountForMonth(id);
    }

    @DeleteMapping("/{id}")
    public Long deleteMonth(@PathVariable Long id){
        return monthService.deleteMonthById(id);
    }
}
