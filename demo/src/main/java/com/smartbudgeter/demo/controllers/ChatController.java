package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.ChatMessage;
import com.smartbudgeter.demo.repositories.ChatMessageRepository;
import com.smartbudgeter.demo.services.ChatService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository messageRepo;

    @PostMapping
    public ResponseEntity<?> postUserMessage(@RequestBody ChatMessage userMessage) {
        try {
            String sender = userMessage.getSender();
            String text = userMessage.getText();
            Integer userId = userMessage.getUserId(); // Get the actual user ID

            // Validate input
            if (userId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required"));
            }

            if (sender == null || text == null || sender.trim().isEmpty() || text.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Sender and text are required"));
            }

            // Save user message with userId
            ChatMessage savedUserMessage = new ChatMessage(sender, text, null, userId);
            savedUserMessage = messageRepo.save(savedUserMessage);

            // Detect intent
            String intent = chatService.detectIntentLLM(text);
            System.out.println("INTENT DETECTED222: " + intent);

            // Update user message with detected intent
            savedUserMessage.setIntent(intent);
            messageRepo.save(savedUserMessage);

            // Process response based on actual user ID
            String botReply = chatService.processUserIntent(userId, text);
            
            ChatMessage botMessage = new ChatMessage("bot", botReply, null);
            botMessage = messageRepo.save(botMessage);

            return ResponseEntity.ok(botMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

}
