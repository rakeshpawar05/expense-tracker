package com.project.expenseTracker.service;

import com.project.expenseTracker.dto.ExpenseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIResponseService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateResponse(String userQuery, List<ExpenseDto> expenses) {
        // Placeholder for AI response generation logic
        log.info("Generating AI response for query: {}", userQuery);

        String prompt = buildPrompt(userQuery, expenses);

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

        return extractContent(response);
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

        List choices = (List) body.get("choices");
        Map choice = (Map) choices.get(0);

        Map message = (Map) choice.get("message");

        return (String) message.get("content");
    }
}
