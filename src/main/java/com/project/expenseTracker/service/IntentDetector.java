package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.IntentResult;
import com.project.expenseTracker.enums.IntentType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class IntentDetector {

    public IntentResult detect(String message) {

        String msg = message.toLowerCase();

        IntentResult result = new IntentResult();

        // INTENT DETECTION
        if (msg.contains("total") || msg.contains("spent")) {
            result.setIntent(IntentType.TOTAL_EXPENSE);
        }
        else if (msg.contains("top")) {
            result.setIntent(IntentType.TOP_EXPENSES);
        }
        else if (msg.contains("category")) {
            result.setIntent(IntentType.CATEGORY_SUMMARY);
        }
        else {
            result.setIntent(IntentType.UNKNOWN);
        }

        // PARAMETER EXTRACTION (basic)
        result.setMonth(extractMonth(msg));

        return result;
    }

    private String extractMonth(String msg) {

        LocalDate now = LocalDate.now();

        if (msg.contains("last month")) {
            LocalDate lastMonth = now.minusMonths(1);
            return formatMonth(lastMonth);
        }

        if (msg.contains("this month")) {
            return formatMonth(now);
        }

        // fallback
        return formatMonth(now);
    }

    private String formatMonth(LocalDate date) {

        String month = date.getMonth().toString().substring(0,1)
                + date.getMonth().toString().substring(1).toLowerCase();

        return month + "," + date.getYear();
    }
}