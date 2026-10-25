package com.project.expenseTracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.expenseTracker.dto.AIInsightDto;
import com.project.expenseTracker.dto.ExpenseDto;
import com.project.expenseTracker.dto.ParsedExpenseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AIResponseService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model:openai/gpt-oss-20b}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String userQuery, List<ExpenseDto> expenses) {
        log.info("Generating AI response for query: {}", userQuery);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Groq API key is missing. Generating structured local response.");
            return generateLocalFallback(userQuery, expenses);
        }

        try {
            String prompt = buildPrompt(userQuery, expenses);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "You are an intelligent, helpful financial advisor for personal expense tracking. " +
                            "Provide clear, concise, and actionable responses based strictly on the provided user financial data. " +
                            "Always format currency amounts in Indian Rupees (₹) with comma separators (e.g., ₹25,000). " +
                            "When presenting category breakdowns, comparisons, or multiple transactions, format them using clean GitHub Markdown tables with clear column headers. " +
                            "Highlight key totals, figures, and dates in bold (e.g., **₹90,498**). " +
                            "Include a quick, helpful insight or observation when relevant."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            return extractContent(response);
        } catch (Exception e) {
            log.warn("Groq API call failed ({}). Generating structured local fallback response.", e.getMessage());
            return generateLocalFallback(userQuery, expenses);
        }
    }

    public AIInsightDto generateMonthlyInsight(
            String title,
            String periodType,
            YearMonth analyzedYm,
            int currentDayOfMonth,
            Double totalEarning,
            Double totalExpense,
            Double balance,
            Double spendPercentage,
            int transactionCount,
            String topCategory,
            Double topCategoryAmount,
            Map<String, Double> categoryTotals,
            String defaultStatusTag
    ) {
        AIInsightDto.AIInsightDtoBuilder builder = AIInsightDto.builder()
                .title(title)
                .periodType(periodType)
                .analyzedYearMonth(analyzedYm)
                .totalEarning(totalEarning)
                .totalExpense(totalExpense)
                .balance(balance)
                .spendPercentage(spendPercentage)
                .topCategory(topCategory)
                .topCategoryAmount(topCategoryAmount)
                .statusTag(defaultStatusTag);

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String systemPrompt = "You are an elite personal financial advisor. Analyze the monthly user financial data and produce a high-value assessment. Format your response strictly as valid JSON with no markdown wrapping and keys: headline, insight, keyTakeaway, statusTag.";
                String userPrompt = String.format("""
                        Financial Data:
                        - Period: %s (%s)
                        - Period Type: %s (PREVIOUS_MONTH_WRAPUP = completed month; CURRENT_MONTH_PACING = live on day %d)
                        - Income: ₹%,.0f
                        - Total Spent: ₹%,.0f
                        - Net Balance: ₹%,.0f
                        - Spend Percentage: %.1f%%
                        - Transaction Count: %d
                        - Top Spending Category: %s (₹%,.0f)
                        - Category Breakdown: %s

                        Produce JSON matching this exact structure:
                        {
                          "headline": "A punchy 5-8 word headline (e.g. 'Controlled Spending with Healthy Surplus Buffer')",
                          "insight": "A crisp, professional 2-sentence insight with ₹ figures.",
                          "keyTakeaway": "One clear 1-sentence actionable advice or projection.",
                          "statusTag": "%s"
                        }
                        """,
                        title, analyzedYm, periodType, currentDayOfMonth,
                        totalEarning, totalExpense, balance, spendPercentage,
                        transactionCount, topCategory, topCategoryAmount, categoryTotals, defaultStatusTag
                );

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelName);
                requestBody.put("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ));
                requestBody.put("temperature", 0.3);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                String content = extractContent(response);

                String cleanedJson = cleanJsonContent(content);
                JsonNode root = objectMapper.readTree(cleanedJson);

                if (root.has("headline") && !root.get("headline").asText().isBlank()) {
                    builder.headline(root.get("headline").asText());
                }
                if (root.has("insight") && !root.get("insight").asText().isBlank()) {
                    builder.insight(root.get("insight").asText());
                }
                if (root.has("keyTakeaway") && !root.get("keyTakeaway").asText().isBlank()) {
                    builder.keyTakeaway(root.get("keyTakeaway").asText());
                }
                if (root.has("statusTag") && !root.get("statusTag").asText().isBlank()) {
                    builder.statusTag(root.get("statusTag").asText());
                }

                AIInsightDto result = builder.build();
                if (result.getHeadline() != null && result.getInsight() != null) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("Groq monthly insight call failed ({}). Using deterministic financial analysis.", e.getMessage());
            }
        }

        return generateDeterministicMonthlyInsight(builder, periodType, analyzedYm, currentDayOfMonth,
                totalEarning, totalExpense, balance, spendPercentage, transactionCount, topCategory, topCategoryAmount);
    }

    private AIInsightDto generateDeterministicMonthlyInsight(
            AIInsightDto.AIInsightDtoBuilder builder,
            String periodType,
            YearMonth analyzedYm,
            int currentDayOfMonth,
            Double totalEarning,
            Double totalExpense,
            Double balance,
            Double spendPercentage,
            int transactionCount,
            String topCategory,
            Double topCategoryAmount
    ) {
        String monthName = analyzedYm.getMonth().name().substring(0, 1).toUpperCase() +
                analyzedYm.getMonth().name().substring(1).toLowerCase();

        String headline;
        String insight;
        String keyTakeaway;

        if (totalEarning <= 0 && totalExpense <= 0) {
            headline = "No Financial Activity Logged Yet";
            insight = String.format("No expenses or income entries are logged for %s %d.", monthName, analyzedYm.getYear());
            keyTakeaway = "Record your monthly salary/income to see budget utilization.";
        } else if ("PREVIOUS_MONTH_WRAPUP".equals(periodType)) {
            if (balance >= 0) {
                headline = String.format("Closed %s with a ₹%,.0f Surplus", monthName, balance);
                insight = String.format("You concluded %s %d spending ₹%,.0f (%.1f%% of income) across %d transactions. Your top expenditure was %s at ₹%,.0f.",
                        monthName, analyzedYm.getYear(), totalExpense, spendPercentage, transactionCount, topCategory, topCategoryAmount);
                keyTakeaway = "Strong budget discipline preserved your net surplus into the new month.";
            } else {
                headline = String.format("%s Deficit: Overspent by ₹%,.0f", monthName, Math.abs(balance));
                insight = String.format("In %s %d, total spend of ₹%,.0f exceeded your income by ₹%,.0f. %s formed the largest chunk at ₹%,.0f.",
                        monthName, analyzedYm.getYear(), totalExpense, Math.abs(balance), topCategory, topCategoryAmount);
                keyTakeaway = "Consider reviewing non-essential spending to re-balance this month.";
            }
        } else if ("CURRENT_MONTH_PACING".equals(periodType)) {
            if (spendPercentage > 80) {
                headline = String.format("High Burn Rate: %.1f%% of Budget Spent", spendPercentage);
                insight = String.format("Day %d of %s: You have used ₹%,.0f of your ₹%,.0f income across %d transactions. %s is your top outflow at ₹%,.0f.",
                        currentDayOfMonth, monthName, totalExpense, totalEarning, transactionCount, topCategory, topCategoryAmount);
                keyTakeaway = "Slow down discretionary spending over the next week to avoid a deficit.";
            } else {
                headline = String.format("Healthy Pacing: %.1f%% Income Utilized", spendPercentage);
                insight = String.format("Day %d of %s: Spending is steady at ₹%,.0f across %d transactions. Leading category is %s (₹%,.0f).",
                        currentDayOfMonth, monthName, totalExpense, transactionCount, topCategory, topCategoryAmount);
                keyTakeaway = String.format("On track! You currently maintain a ₹%,.0f buffer.", balance);
            }
        } else {
            headline = String.format("%s %d Financial Overview", monthName, analyzedYm.getYear());
            insight = String.format("Total expenditures reached ₹%,.0f across %d transaction(s) with a remaining balance of ₹%,.0f. Major category: %s (₹%,.0f).",
                    totalExpense, transactionCount, balance, topCategory, topCategoryAmount);
            keyTakeaway = "Historical summary analyzed based on recorded transactions.";
        }

        return builder
                .headline(headline)
                .insight(insight)
                .keyTakeaway(keyTakeaway)
                .build();
    }

    private String cleanJsonContent(String content) {
        if (content == null) return "{}";
        String cleaned = content.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String generateLocalFallback(String userQuery, List<ExpenseDto> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return "I couldn't find any recorded expenses matching your query. Try adding expenses or selecting another month.";
        }

        int total = expenses.stream().mapToInt(e -> e.getAmount() != null ? e.getAmount() : 0).sum();
        ExpenseDto highest = expenses.stream()
                .max((a, b) -> Integer.compare(a.getAmount() != null ? a.getAmount() : 0, b.getAmount() != null ? b.getAmount() : 0))
                .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("You recorded **%d** transaction(s) totaling **₹%,d**.\n\n", expenses.size(), total));
        if (highest != null) {
            sb.append(String.format("- **Top expense:** %s (**₹%,d** on %s)\n", highest.getDescription(), highest.getAmount(), highest.getDate()));
        }

        return sb.toString();
    }

    private String buildPrompt(String userQuery, List<ExpenseDto> expenses) {
        String expensesStr = expenses.isEmpty() ? "No expenses found." :
                expenses.stream()
                        .map(e -> String.format("%s: ₹%d on %s (category: %s) (Event: %s)",
                                e.getDescription(), e.getAmount(), e.getDate(), e.getCategoryName(), e.getEventName()))
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("");

        return """
        You are an assistant that helps users understand their expenses.
        User query: %s
        Here are the filtered expenses for the user query: %s
        
        Your task is to generate a concise and informative response based on
        the user query and the provided expenses. Focus on providing insights,
        summaries, or answers that directly address the user's question about their expenses.

        Your response should be clear, concise, and relevant to the user's query.
        Avoid unnecessary details and focus on delivering value based on the provided expenses.
        No hallucinations, only use the provided expenses to generate the response.
        """.formatted(userQuery, expensesStr);
    }

    private String extractContent(ResponseEntity<Map> response) {
        Map body = response.getBody();
        if (body == null) return "No response received.";

        List choices = (List) body.get("choices");
        if (choices == null || choices.isEmpty()) return "No response choices available.";

        Map choice = (Map) choices.get(0);
        if (choice == null) return "Empty choice in response.";

        Map message = (Map) choice.get("message");
        if (message == null || message.get("content") == null) return "No message content found.";

        return (String) message.get("content");
    }

    public ParsedExpenseDto parseExpenseText(
            String text,
            String todayDate,
            List<String> availableCategories,
            List<String> availableEvents
    ) {
        if (text == null || text.isBlank()) {
            return ParsedExpenseDto.builder().build();
        }

        String effectiveDate = (todayDate != null && !todayDate.isBlank())
                ? todayDate
                : LocalDate.now().toString();

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String systemPrompt = "You are a specialized financial transaction entity extractor. Extract the expense details from the user's natural language or bank/UPI transaction SMS text. Return strictly a JSON object with keys: description, amount (integer), date (YYYY-MM-DD), categoryName, eventName (or null if none). No markdown wrapping, no extra keys.";

                String userPrompt = String.format("""
                        Reference Information:
                        - Today's date: %s
                        - User's existing categories: %s
                        - User's existing events: %s

                        Text to parse:
                        "%s"

                        Extraction Instructions:
                        1. "description": Clean merchant, vendor, or item name (e.g., "Social", "Uber", "Swiggy", "Grocery", "Movie Tickets"). Avoid generic prefixes like "Paid to", "spent on", "debited for".
                        2. "amount": Pure integer number in Rupees (e.g., 1450). Remove any currency symbols (₹, Rs, INR) or commas.
                        3. "date": ISO format "YYYY-MM-DD". If "yesterday", compute relative to today's date (%s). If a specific date like "24-Oct" is given, use the year from today's date. If no date is mentioned, use "%s".
                        4. "categoryName": Choose the best matching category. Prefer one from the existing categories list if relevant; otherwise choose a standard category (e.g., Food & Dining, Travel, Bills & Utilities, Shopping, Entertainment, Health & Fitness, Grocery, General).
                        5. "eventName": If the text refers to a trip, event, vacation, or festival (e.g., "Goa trip", "Diwali", "Wedding"), map it to the closest existing event or provide the event name. If no event is referenced, return null.

                        Output JSON format:
                        {
                          "description": "...",
                          "amount": 1000,
                          "date": "%s",
                          "categoryName": "...",
                          "eventName": null
                        }
                        """,
                        effectiveDate,
                        availableCategories != null && !availableCategories.isEmpty() ? String.join(", ", availableCategories) : "None",
                        availableEvents != null && !availableEvents.isEmpty() ? String.join(", ", availableEvents) : "None",
                        text.trim(),
                        effectiveDate,
                        effectiveDate,
                        effectiveDate
                );

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", modelName);
                requestBody.put("messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ));
                requestBody.put("temperature", 0.0);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                String content = extractContent(response);

                String cleanedJson = cleanJsonContent(content);
                JsonNode root = objectMapper.readTree(cleanedJson);

                String description = root.has("description") ? root.get("description").asText().trim() : null;
                Integer amount = root.has("amount") && !root.get("amount").isNull() ? root.get("amount").asInt() : null;
                String date = root.has("date") && !root.get("date").isNull() ? root.get("date").asText().trim() : effectiveDate;
                String categoryName = root.has("categoryName") && !root.get("categoryName").isNull() ? root.get("categoryName").asText().trim() : "General";
                String eventName = root.has("eventName") && !root.get("eventName").isNull() ? root.get("eventName").asText().trim() : null;
                if ("null".equalsIgnoreCase(eventName) || (eventName != null && eventName.isBlank())) {
                    eventName = null;
                }

                if (description != null && !description.isBlank() && amount != null && amount > 0) {
                    return ParsedExpenseDto.builder()
                            .description(description)
                            .amount(amount)
                            .date(date != null && !date.isBlank() ? date : effectiveDate)
                            .categoryName(categoryName)
                            .eventName(eventName)
                            .confidence(0.95)
                            .rawInput(text)
                            .build();
                }
            } catch (Exception e) {
                log.warn("Groq expense parsing failed ({}). Falling back to heuristic rule-based parser.", e.getMessage());
            }
        }

        return fallbackParseExpense(text, effectiveDate, availableCategories, availableEvents);
    }

    private ParsedExpenseDto fallbackParseExpense(
            String text,
            String effectiveDate,
            List<String> availableCategories,
            List<String> availableEvents
    ) {
        String lower = text.toLowerCase();

        // 1. Extract Amount
        Integer amount = null;
        Pattern amountPattern = Pattern.compile("(?i)(?:₹|rs\\.?|inr)\\s*([\\d,]+(?:\\.\\d+)?)|([\\d,]+(?:\\.\\d+)?)\\s*(?:₹|rs\\.?|inr)");
        Matcher matcher = amountPattern.matcher(text);
        if (matcher.find()) {
            String val = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            val = val.replace(",", "");
            try {
                amount = (int) Math.round(Double.parseDouble(val));
            } catch (Exception ignored) {}
        }

        if (amount == null) {
            Pattern numberPattern = Pattern.compile("\\b(\\d{2,6})\\b");
            Matcher numMatcher = numberPattern.matcher(text);
            if (numMatcher.find()) {
                try {
                    amount = Integer.parseInt(numMatcher.group(1));
                } catch (Exception ignored) {}
            }
        }

        if (amount == null) {
            amount = 100;
        }

        // 2. Extract Date
        String resolvedDate = effectiveDate;
        try {
            LocalDate today = LocalDate.parse(effectiveDate);
            if (lower.contains("yesterday")) {
                resolvedDate = today.minusDays(1).toString();
            } else if (lower.contains("today")) {
                resolvedDate = today.toString();
            } else {
                Pattern datePattern = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
                Matcher dm = datePattern.matcher(text);
                if (dm.find()) {
                    resolvedDate = dm.group(1);
                }
            }
        } catch (Exception ignored) {}

        // 3. Extract Category
        String category = "General";
        if (availableCategories != null) {
            for (String cat : availableCategories) {
                if (lower.contains(cat.toLowerCase())) {
                    category = cat;
                    break;
                }
            }
        }

        if ("General".equals(category)) {
            if (lower.contains("swiggy") || lower.contains("zomato") || lower.contains("dinner") || lower.contains("lunch")
                    || lower.contains("food") || lower.contains("coffee") || lower.contains("starbucks") || lower.contains("burger")) {
                category = "Food";
            } else if (lower.contains("uber") || lower.contains("ola") || lower.contains("flight") || lower.contains("petrol")
                    || lower.contains("fuel") || lower.contains("train") || lower.contains("travel")) {
                category = "Travel";
            } else if (lower.contains("netflix") || lower.contains("movie") || lower.contains("cinema") || lower.contains("game")) {
                category = "Entertainment";
            } else if (lower.contains("amazon") || lower.contains("flipkart") || lower.contains("myntra") || lower.contains("shopping")) {
                category = "Shopping";
            } else if (lower.contains("wifi") || lower.contains("electricity") || lower.contains("rent") || lower.contains("recharge") || lower.contains("bill")) {
                category = "Bills";
            } else if (lower.contains("doctor") || lower.contains("medicine") || lower.contains("hospital") || lower.contains("pharmacy")) {
                category = "Health";
            } else if (lower.contains("grocery") || lower.contains("blinkit") || lower.contains("zepto") || lower.contains("bigbasket")) {
                category = "Grocery";
            }
        }

        // 4. Extract Event
        String eventName = null;
        if (availableEvents != null) {
            for (String ev : availableEvents) {
                if (lower.contains(ev.toLowerCase())) {
                    eventName = ev;
                    break;
                }
            }
        }

        // 5. Clean Description
        String description = text
                .replaceAll("(?i)(?:₹|rs\\.?|inr)\\s*[\\d,]+(?:\\.\\d+)?", "")
                .replaceAll("(?i)[\\d,]+(?:\\.\\d+)?\\s*(?:₹|rs\\.?|inr)", "")
                .replaceAll("(?i)\\b(paid to|paid|debited by|debited for|spent on|using upi|acct xx\\d+|ref no:?\\s*\\w+)\\b", "")
                .replaceAll("(?i)\\b(yesterday|today|tomorrow)\\b", "")
                .replaceAll("(?i)\\bfor\\s+\\w+\\s+trip\\b", "")
                .trim();

        if (description.isBlank()) {
            description = "Expense";
        } else if (description.length() > 40) {
            description = description.substring(0, 40).trim();
        }

        return ParsedExpenseDto.builder()
                .description(description)
                .amount(amount)
                .date(resolvedDate)
                .categoryName(category)
                .eventName(eventName)
                .confidence(0.75)
                .rawInput(text)
                .build();
    }
}
