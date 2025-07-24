package com.smartbudgeter.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GroqClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key}")
    private String API_KEY;

    public String getChatbotReply(List<Map<String, String>> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // Ensure system message exists
        boolean hasSystem = messages.stream().anyMatch(m -> "system".equals(m.get("role")));
        if (!hasSystem) {
            messages.add(0, Map.of(
                    "role", "system",
                    "content", "You are an expense tracking assistant. Extract amount and category if user adds expense. Summarize expenses if asked.-if the user does not specifcy the category ask him to specify if he deos not do nothing- if currency is absent always assume tnd as currency.-if user asks for summary give him the total amount of expenses in tnd and the number of expenses in the last month.-if user asks for a specific date give him the total amount of expenses in tnd and the number of expenses in that date.-if user asks for a specific category give him the total amount of expenses in tnd and the number of expenses in that category.-if user asks for a specific month give him the total amount of expenses in tnd and the number of expenses in that month.- don't talk for too long but keep it friendly- only reply with the details for the last request then don't mention them again in the next message-if the user asks for a reminder make sure to get the name and due date-if the user wishes to add or review a budget make sure to ask for category-you should also be able to add alerts."
            ));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error communicating with Groq API.";
        }
    }
    public String detectIntentLLM(String userMessage) {
        String prompt = buildPrompt(userMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
            "model", "meta-llama/llama-4-scout-17b-16e-instruct",
            "messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            },
            "temperature", 0.0
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            Map completion = (Map) ((List<?>) response.getBody().get("choices")).get(0);
            Map message = (Map) completion.get("message");
            System.out.println("🤖111 "+ message);
            return message.get("content").toString().trim().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    private String buildPrompt(String userMessage) {
        return String.format("""
            You are a helpful assistant. Classify the user's intent based on their message.
                Possible intents:
                - add_expense
                - list_expenses
                - give_summary
                - spending_per_period
                - add_alert
                - give_reminder
                - review_budget
                - add_budget
                - add_money
                - filter_expenses
                - delete_expense

                Respond with only the intent name.

                Examples:

                User: I just bought groceries for 30 dinars  
                Intent: add_expense

                User: Show me all my expenses  
                Intent: list_expenses

                User: Give me a quick overview of my finances  
                Intent: give_summary

                User: How much did I spend in June?  
                Intent: spending_per_period

                User: Warn me if I spend more than 100 on food  
                Intent: add_alert

                User: Remind me to pay rent next week  
                Intent: give_reminder

                User: Can you check if I stayed within budget this month?  
                Intent: review_budget

                User: Set my monthly budget to 500  
                Intent: add_budget

                User: I just got paid, add 1000 to my balance  
                Intent: add_money

                User: Show only my restaurant expenses  
                Intent: filter_expenses

                User: Delete the expense for the Uber ride yesterday  
                Intent: delete_expense

                User: I bought snacks for 10  
                Intent: add_expense

                User: I spent 30 dollars on food yesterday  
                Intent: add_expense

                User: Show me what I’ve spent this month  
                Intent: total_spent

                User: Can I see my latest expenses?  
                Intent: list_expenses

                User: Hello!  
                Intent: greeting

                User: What can you do?  
                Intent: help

                User: Blah blah blah  
                Intent: unknown

                User message: "%s"
                Intent:
        """, userMessage);
    }
}
