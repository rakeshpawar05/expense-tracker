package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.SavingDto;
import com.project.expenseTracker.service.SavingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/savings")
@AllArgsConstructor
public class SavingController {

    private final SavingService savingService;

    @GetMapping("/{id}")
    public SavingDto getSavingById(@PathVariable("id") Long id){
        return savingService.getSavingById(id);
    }

    @GetMapping
    public List<SavingDto> getSavings(@RequestParam("userId") Long userId,
                                        @RequestParam(name = "yearMonth", required = false) YearMonth yearMonth,
                                        @RequestParam(name = "categoryName", required = false) String categoryName,
                                        @RequestParam(name = "savingName", required = false) String savingName){
        return savingService.getSavings(userId, yearMonth, categoryName, savingName);
    }

    @PostMapping
    public long createSaving(@RequestBody SavingDto savingDto){
        return savingService.createSaving(savingDto);
    }

    @PutMapping("/{id}")
    public SavingDto updateSaving(@RequestBody SavingDto savingDto, @PathVariable("id") Long id){
        return savingService.updateSaving(savingDto, id);
    }

    @DeleteMapping("/{id}")
    public long deleteSaving(@PathVariable("id") Long id){
        return savingService.deleteSavingById(id);
    }

    @GetMapping("/top5")
    public List<SavingDto> getTop5ByMonth(@RequestParam("userId") Long userId, @RequestParam("yearMonth") YearMonth yearMonth) {
        return savingService.getTop5ByMonth(userId, yearMonth);
    }
}
