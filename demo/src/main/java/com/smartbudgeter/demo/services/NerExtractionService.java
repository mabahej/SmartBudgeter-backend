package com.smartbudgeter.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

@Service
public class NerExtractionService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final String HF_URL = "https://api-inference.huggingface.co/models/Davlan/bert-base-multilingual-cased-ner-hrl";

    public List<Map<String, String>> extractEntities(String inputText) {
        try {
            String payload = "{ \"inputs\": \"" + inputText + "\" }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(HF_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonArray = mapper.readTree(response.body());

            List<Map<String, String>> results = new ArrayList<>();

            for (JsonNode entity : jsonArray) {
                Map<String, String> item = new HashMap<>();
                item.put("entity", entity.get("entity_group").asText()); // e.g. PER, LOC, MISC, ORG
                item.put("word", entity.get("word").asText());
                results.add(item);
            }

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
        public Map<String, Object> extractSlots(String message) {
        List<Map<String, String>> entities = extractEntities(message);
        Map<String, Object> result = new HashMap<>();

        // Enhanced amount detection (handles decimals and currency symbols)
        for (Map<String, String> ent : entities) {
            String word = ent.get("word");
            String entity = ent.get("entity");

            // Handle amounts (now supports decimal numbers)
            if (word.matches("[0-9]+(\\.[0-9]+)?") && !result.containsKey("amount")) {
                result.put("amount", Float.parseFloat(word));
            } 
            // Handle categories (from more entity types)
            else if ((entity.equals("MISC") || entity.equals("PROD")) && !result.containsKey("category")) {
                result.put("category", word);
            }
        }

        // Enhanced date detection
        LocalDate today = LocalDate.now();
        if (message.toLowerCase().contains("hier") || message.toLowerCase().contains("yesterday")) {
            result.put("date", today.minusDays(1));
        } else if (message.toLowerCase().contains("aujourd'hui") || 
                  message.toLowerCase().contains("today")) {
            result.put("date", today);
        } else if (message.toLowerCase().contains("demain") || 
                  message.toLowerCase().contains("tomorrow")) {
            result.put("date", today.plusDays(1));
        }
        // Could add more date patterns here

        return result;
    }

}
