package com.project.expenseTracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.expenseTracker.dto.IntentResult;
import com.project.expenseTracker.enums.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIIntentService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public IntentResult detect(String message) {

        String prompt = buildPrompt(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", "You extract structured JSON from user queries."
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

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(apiUrl, entity, Map.class);

        String content = extractContent(response);

        return parseResponse(content);
    }

    private String buildPrompt(String message) {
        return """
You are an AI assistant that extracts structured data from user queries.

Return ONLY JSON.

Supported intents:
- TOTAL_EXPENSE
- TOP_EXPENSES
- CATEGORY_SUMMARY

Extract:
- intent
- month (this_month, last_month, or actual month like January,2025 if specified)
- category (if any)

User: "%s"

Response format:
{
  "intent": "TOTAL_EXPENSE",
  "month": "last_month",// or "January,2025"
  "category": null
}
""".formatted(message);
    }

    private String extractContent(ResponseEntity<Map> response) {

        Map body = response.getBody();

        List choices = (List) body.get("choices");
        Map choice = (Map) choices.get(0);

        Map message = (Map) choice.get("message");

        return (String) message.get("content");
    }

    private IntentResult parseResponse(String json) {

        ObjectMapper mapper = new ObjectMapper();

        try {
            json = cleanJson(json);
            log.info("Cleaned JSON: {}", json);
            IntentResult intentResult = mapper.readValue(json, IntentResult.class);
            intentResult.setMonth(resolveMonth(intentResult.getMonth()));
            log.info("Parsed intent: {}", intentResult);
            return intentResult;
        } catch (Exception e) {
            return new IntentResult(IntentType.UNKNOWN, null, null);
        }
    }

    private String cleanJson(String content) {

        // remove markdown
        content = content.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // extract only JSON block
        content = content.replaceAll("(?s).*?(\\{.*\\}).*", "$1");

        return content;
    }

    private String resolveMonth(String monthKey) {

        LocalDate now = LocalDate.now();

        if ("last_month".equalsIgnoreCase(monthKey)) {
            return formatMonth(now.minusMonths(1));
        }

        if ("this_month".equalsIgnoreCase(monthKey) || "current_month".equalsIgnoreCase(monthKey)) {
            return formatMonth(now);
        }

        // fallback (if AI gives actual month later)
        return monthKey;
    }

    private String formatMonth(LocalDate date) {

        String month = date.getMonth().toString().substring(0,1)
                + date.getMonth().toString().substring(1).toLowerCase();

        return month + "," + date.getYear();
    }
}
