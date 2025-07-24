package com.smartbudgeter.demo.services;

import com.smartbudgeter.demo.models.*;
import com.smartbudgeter.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.regex.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository messageRepo;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private ExpenseRepository expenseRepository;
    private static final List<String> categories = List.of(
    "food", "groceries", "coffee", "transport", "rent", "entertainment", "bills", "shopping", "restaurant"
);


    @Autowired
    private GroqClient groqClient;

    public String processUserMessage(String sender, String userText) {
        // 1. Load last 2 messages from sender (descending order)
        List<ChatMessage> lastTwo = messageRepo.findTop2BySenderOrderByIdDesc(sender);
        System.out.println("Last two messages: " + lastTwo.iterator().next().getText() + ", " + lastTwo.get(1).getText());
        if (lastTwo.size() < 2) {
            // If less than 2 messages, return a default response
            return "Je n'ai pas assez d'informations pour répondre.";
        }
        // 2. Build message list for Groq in correct order (oldest first)
        List<Map<String, String>> messages = lastTwo.stream()
                .sorted(Comparator.comparing(ChatMessage::getId))
                .map(m -> Map.of(
                        "role", m.getSender().equals("bot") ? "assistant" : "user",
                        "content", m.getText()
                ))
                .collect(Collectors.toList());

        // 3. Add current user message
        messages.add(Map.of("role", "user", "content", userText));

        // 4. Call Groq
        return groqClient.getChatbotReply(messages);
    }
    public String detectIntentLLM(String message) {
        
        return groqClient.detectIntentLLM(message);
    }
    public String processUserIntent(int id, String message) {
        String intent = detectIntentLLM(message); // already done earlier

        switch (intent) {
            case "add_expense":
                handleAddExpense(id, message);
            case "list_expenses":
               // return handleListExpenses(sender);
            case "give_summary":
               // return handleGiveSummary(sender);
            case "spending_per_period":
             //   return handleSpendingPerPeriod(sender, message);
            case "add_alert":
             //   return handleAddAlert(sender, message);
            case "give_reminder":
             //   return handleGiveReminder(sender);
            case "review_budget":
             //   return handleReviewBudget(sender);
            case "add_budget":
             //   return handleAddBudget(sender, message);
            case "add_money":
             //   return handleAddMoney(sender, message);
            case "filter_expenses":
             //   return handleFilterExpenses(sender, message);
            case "delete_expense":
             //   return handleDeleteExpense(sender, message);
            default:
                return "Sorry, I didn't understand that. Could you rephrase?";
        }
    }
    private String handleAddExpense(int id, String message) {
        // 1. Extract amount, categoryName, date (as before)
        double amount =extractAmount(message);
        String categoryName = extractCategory(message);
        LocalDate date = extractDate(message);

        if (amount <= 0) {
            return "Sorry, I couldn't find a valid amount.";
        }

        // 2. Load user entity from userRepository (by username or id)
        User user = userRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        if (user == null) {
            return "User not found.";
        }

        // 3. Find category entity by name and user, or create if doesn't exist
        Category category = categoryRepository.findByNameAndUser(categoryName, user);
        if (category == null) {
            category = new Category();
            category.setName(categoryName);
            category.setUser(user); // Set the user for the category
            categoryRepository.save(category);
        }

        // 4. Create and save the expense
        Expense expense = new Expense();
        expense.setAmount((float) amount);
        expense.setCategory(category);
        expense.setUser(user);
        expense.setDate(date); // Use provided date or today
        expenseRepository.save(expense);

        return String.format("Added %.2f TND expense for category '%s' on %s.", amount, categoryName, date);
    }
    
    private double extractAmount(String message) {
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.0; // fallback
    }
        
    private String extractCategory(String message) {
    String lowerMsg = message.toLowerCase();
    for (String category : categories) {
        if (lowerMsg.contains(category)) {
            return category;
        }
    }
    return "miscellaneous"; // default category
}

    private LocalDate extractDate(String message) {
        String lowerMsg = message.toLowerCase();
        if (lowerMsg.contains("yesterday")) {
            return LocalDate.now().minusDays(1);
        } else if (lowerMsg.contains("today")) {
            return LocalDate.now();
        } else {
            // Could add more parsing, for now default to today
            return LocalDate.now();
        }
    }

}



