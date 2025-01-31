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

//    @GetMapping("/name")
//    public MonthDto getMonthByName(@RequestParam Long userId,@RequestParam String monthName){
//        return monthService.getMonthByName(userId, monthName);
//    }

    @GetMapping
    public List<MonthDto> getMonths(@RequestParam Long userId, @RequestParam(required = false) String monthName){
        return monthService.getMonths(userId, monthName);
    }

    @PostMapping
    public long createMonth(@RequestBody MonthDto monthDto){
        return monthService.createMonth(monthDto);
    }

    @PutMapping("/{id}")
    public MonthDto updateMonth(@RequestBody MonthDto monthDto, @PathVariable Long id){
        return monthService.updateMonth(id, monthDto);
    }

    @GetMapping("/getNames")
    public List<String> getMonthNamesByUserId(@RequestParam Long userId){
       return monthService.getMonthNamesByUserId(userId);
    }

//    @GetMapping("/{id}/amount")
//    public int getAmountForMonth(@PathVariable Long id){
//        return monthService.getAmountForMonth(id);
//    }

    @DeleteMapping("/{id}")
    public Long deleteMonth(@PathVariable Long id){
        return monthService.deleteMonthById(id);
    }
}
