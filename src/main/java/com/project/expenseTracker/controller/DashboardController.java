package com.project.expenseTracker.controller;

import com.project.expenseTracker.dto.DashboardDto;
import com.project.expenseTracker.service.DashboardService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get complete dashboard data for the current user
     * Returns: month summary, total earnings, total expenses, balance, top 5 expenses, and category breakdown
     *
     * @param yearMonth format: "MonthName,year" (e.g., "January,2025")
     * @return DashboardDto with complete dashboard information
     */
    @GetMapping("/complete")
    public DashboardDto getDashboardComplete(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "yearMonth") YearMonth yearMonth) {
        return dashboardService.getDashboardData(userId, yearMonth);
    }
}

