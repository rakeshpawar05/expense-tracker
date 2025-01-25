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
        return monthService.mapDtoToEntity(monthDto);
    }

    @PutMapping("/{id}")
    public MonthDto updateMonth(@RequestBody MonthDto monthDto, @PathVariable Long id){
        return monthService.updateMonth(id, monthDto);
    }

    @DeleteMapping("/{id}")
    public Long deleteMonth(@PathVariable Long id){
        return monthService.deleteMonthById(id);
    }
}
