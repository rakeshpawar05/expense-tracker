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

    @Value("${groq.api.model:openai/gpt-oss-20b}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

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
}
