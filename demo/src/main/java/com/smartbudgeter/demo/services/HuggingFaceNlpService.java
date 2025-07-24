package com.smartbudgeter.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class HuggingFaceNlpService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final String HF_URL = "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";

    public String classifyIntent(String userText) {
    try {
        String[] labels = {
            "add an expense",
            "greet the user",
            "list all expenses for the user",
            "unknown intent"
        };

        String payload = """
            {
              "inputs": "%s",
              "parameters": {
                "candidate_labels": ["%s"]
              }
            }
            """.formatted(userText, String.join("\", \"", labels));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(HF_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        System.out.println("Payload: " + payload);

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response: " + response.body());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(response.body());

        String predictedLabel = json.get("labels").get(0).asText();

        // Map descriptive labels to internal intents
        return switch (predictedLabel) {
            case "add an expense" -> "add_expense";
            case "greet the user" -> "greet";
            case "list all expenses for the user" -> "list_expenses";
            case "unknown intent" -> "unknown";
            default -> "unknown"; // Fallback in case label is unexpected
        };

    } catch (Exception e) {
        e.printStackTrace();
        return "unknown";
    }
}
}
