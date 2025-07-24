package com.smartbudgeter.demo.controllers;

import com.smartbudgeter.demo.models.ChatMessage;
import com.smartbudgeter.demo.repositories.ChatMessageRepository;
import com.smartbudgeter.demo.services.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository messageRepo;

    @PostMapping
    public ChatMessage postUserMessage(@RequestBody ChatMessage userMessage) {
        String sender = userMessage.getSender();
        String text = userMessage.getText();

        String intent = chatService.detectIntentLLM(text);
        System.out.println("INTENT DETECTED222: " + intent);

        // Save user message with detected intent
        ChatMessage savedUserMessage = new ChatMessage(sender, text, intent);
        messageRepo.save(savedUserMessage);

        // Process bot reply
        String botReply = chatService.processUserMessage(sender, text);
        ChatMessage botMessage = new ChatMessage("bot", botReply, null);
        messageRepo.save(botMessage);

        return botMessage;
    }

}
