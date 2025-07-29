package com.smartbudgeter.demo.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.smartbudgeter.demo.repositories.*;
import com.smartbudgeter.demo.models.*;

@Service
public class ChatBotService {

    @Autowired
    private HuggingFaceNlpService nlp;
    @Autowired
    private NerExtractionService ner;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ExpenseRepository expenseRepo;
    /*public String extractIntent(String message) {
        return nlp.classifyIntent(message);
    }

    public String processMessage(String message, String sender) {
        // Save user message
        String intent = extractIntent(message);
        ChatMessage userMsg = new ChatMessage(sender, message, intent);
        chatMessageRepository.save(userMsg);
        
        // Generate and save bot response
        String reply = generateReply(intent, message);
        ChatMessage botMsg = new ChatMessage("bot", reply, intent);
        chatMessageRepository.save(botMsg);
        
        return reply;
    }

    public String generateReply(String intent, String message) {
        if ("add_expense".equals(intent)) {
            var slots = ner.extractSlots(message);
            Float amount = (Float) slots.get("amount");
            String category = (String) slots.get("category");
            var date = slots.get("date");

            if (amount != null && category != null && date != null) {
                return String.format("Dépense ajoutée : %.2f TND pour %s le %s", 
                    amount, category, date.toString());
            } else {
                StringBuilder missing = new StringBuilder("D'accord, mais il manque :");
                if (amount == null) missing.append(" le montant,");
                if (category == null) missing.append(" la catégorie,");
                if (date == null) missing.append(" la date,");
                return missing.substring(0, missing.length()-1) + ".";
            }
        }

        return switch (intent) {
            case "get_summary" -> "Voici un résumé de vos dépenses..."; // More detailed
            case "set_reminder" -> "Quel rappel souhaitez-vous créer ?";
            case "greet" -> "Bonjour ! Comment puis-je vous aider aujourd'hui ?";
            case "get_expenses" -> "Voici la liste de vos dépenses..."; // New case
            default -> "Je n'ai pas bien compris. Pouvez-vous reformuler ?";
        };
    }*/
        private float extractAmount(String text) {
            Pattern p = Pattern.compile("(\\d+(\\.\\d{1,2})?)");
            Matcher m = p.matcher(text);
            if (m.find()) return Float.parseFloat(m.group(1));
            return 0f;
        }

private String extractCategory(String text) {
        String[] commonCategories = {"food", "groceries", "rent", "transport", "coffee", "shopping"};
        for (String category : commonCategories) {
            if (text.toLowerCase().contains(category)) {
                return category;
            }
        }
        return "misc";
    }
    
    public String handleAddExpense(String input, int senderId) {
        float amount = extractAmount(input);
        String category = extractCategory(input);

        User user = userRepo.findById(senderId).orElse(null);
        if (user == null) return "User not found.";

        Expense e = new Expense();
        e.setAmount(amount);
        e.setNote(category);
        e.setUser(user);
        e.setDate(LocalDate.now());
        e.setCreatedAt(LocalDateTime.now());

        expenseRepo.save(e);
        return "Got it! I saved an expense of " + amount + " TND for " + category + ".";
    }


    public String handleListExpenses(int senderId) {
        User user = userRepo.findById(senderId).orElse(null);
        if (user == null) return "User not found.";

        List<Expense> expenses = expenseRepo.findByUser(user);
        if (expenses.isEmpty()) return "You haven't logged any expenses yet.";

        StringBuilder sb = new StringBuilder("Here are your expenses:\n");
        for (Expense e : expenses) {
            sb.append("- ").append(e.getAmount())
            .append(" TND for ").append(e.getNote())
            .append(" on ").append(e.getDate()).append("\n");
        }
        return sb.toString();
    }

}
