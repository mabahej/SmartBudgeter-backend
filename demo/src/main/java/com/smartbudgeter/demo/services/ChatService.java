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
               return handleListExpenses(id);
            case "give_summary":
               return handleGiveSummary(id);
            case "spending_per_period":
                return handleSpendingPerPeriod(id, message);
            case "add_alert":
                return handleAddAlert(id, message);
            case "give_reminder":
                return handleGiveReminder(id);
            case "review_budget":
                return handleReviewBudget(id);
            case "add_budget":
                return handleAddBudget(id, message);
            case "add_money":
                return handleAddMoney(id, message);
            case "filter_expenses":
                return handleFilterExpenses(id, message);
            case "delete_expense":
                //return handleDeleteExpense(id, message);
            case "greeting":
                return "Hello! I'm your budget assistant. How can I help you today?";
            case "help":
                return "I can help you track expenses, view summaries, set budgets, add alerts, and more! Try saying 'Add 20 TND for food' or 'Show me my expenses'.";
            default:
                return "Sorry, I didn't understand that. Could you rephrase? You can ask me to add expenses, view summaries, or set budgets.";
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
        // Try to parse actual dates (DD/MM/YYYY format)
        Pattern datePattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})");
        Matcher matcher = datePattern.matcher(message);
        if (matcher.find()) {
            try {
                // Adjust based on your date format
                String[] parts = matcher.group(1).split("/");
                return LocalDate.of(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            } catch (Exception e) {
                // Fall back to today
            }
        }
    }
    return LocalDate.now();
}

private String handleListExpenses(int userId) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        List<Expense> expenses = expenseRepository.findByUserOrderByDateDesc(user);
        
        if (expenses.isEmpty()) {
            return "You don't have any expenses recorded yet.";
        }
        
        StringBuilder response = new StringBuilder("Here are your recent expenses:\n");
        double total = 0;
        
        for (int i = 0; i < Math.min(5, expenses.size()); i++) {
            Expense expense = expenses.get(i);
            response.append(String.format("- %.2f TND for %s on %s\n", 
                expense.getAmount(), 
                expense.getCategory().getName(), 
                expense.getDate()));
            total += expense.getAmount();
        }
        
        response.append(String.format("\nTotal: %.2f TND (%d expenses)", total, Math.min(5, expenses.size())));
        return response.toString();
    } catch (Exception e) {
        return "Sorry, I couldn't retrieve your expenses. Please try again.";
    }
}

private String handleGiveSummary(int userId) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        List<Expense> recentExpenses = expenseRepository.findByUserAndDateAfterOrderByDateDesc(user, oneMonthAgo);
        
        if (recentExpenses.isEmpty()) {
            return "You haven't recorded any expenses in the last month.";
        }
        
        double total = recentExpenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();
        
        return String.format("In the last month, you've spent %.2f TND across %d expenses.", 
            total, recentExpenses.size());
    } catch (Exception e) {
        return "Sorry, I couldn't generate a summary. Please try again.";
    }
}

private String handleSpendingPerPeriod(int userId, String message) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        // Check for specific date
        LocalDate date = extractDate(message);
        if (!message.toLowerCase().contains("today") && !message.toLowerCase().contains("yesterday")) {
            // Look for specific date in message
            List<Expense> expenses = expenseRepository.findByUserAndDate(user, date);
            if (expenses.isEmpty()) {
                return String.format("You didn't spend anything on %s.", date);
            }
            
            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            return String.format("On %s, you spent %.2f TND across %d expenses.", 
                date, total, expenses.size());
        }
        
        // Default to today
        List<Expense> todayExpenses = expenseRepository.findByUserAndDate(user, LocalDate.now());
        if (todayExpenses.isEmpty()) {
            return "You haven't spent anything today.";
        }
        
        double total = todayExpenses.stream().mapToDouble(Expense::getAmount).sum();
        return String.format("Today, you've spent %.2f TND across %d expenses.", 
            total, todayExpenses.size());
    } catch (Exception e) {
        return "Sorry, I couldn't retrieve spending for that period. Please try again.";
    }
}

private String handleAddAlert(int userId, String message) {
    // This would require an Alert entity and repository
    return "Alert functionality is being set up. I'll notify you when it's ready!";
}

private String handleGiveReminder(int userId) {
    // This would require a Reminder entity and repository
    return "Reminder functionality is being set up. I'll help you remember important dates soon!";
}

private String handleReviewBudget(int userId) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        // This would require Budget entity and logic
        return "Budget review functionality will show you how you're doing against your budget goals!";
    } catch (Exception e) {
        return "Sorry, I couldn't review your budget. Please try again.";
    }
}

private String handleAddBudget(int userId, String message) {
    // This would require Budget entity and repository
    return "Budget setting functionality is being configured. You'll be able to set monthly budgets soon!";
}

private String handleAddMoney(int userId, String message) {
    // This would require Balance/Account entity
    return "Money addition functionality is being set up. You'll be able to track your account balance soon!";
}

private String handleFilterExpenses(int userId, String message) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        String categoryName = extractCategory(message);
        if ("miscellaneous".equals(categoryName)) {
            return "Please specify which category you'd like to filter by (e.g., food, transport, etc.).";
        }
        
        Category category = categoryRepository.findByNameAndUser(categoryName, user);
        if (category == null) {
            return String.format("You don't have any expenses in the '%s' category.", categoryName);
        }
        
        List<Expense> expenses = expenseRepository.findByUserAndCategoryOrderByDateDesc(user, category);
        if (expenses.isEmpty()) {
            return String.format("You don't have any expenses in the '%s' category.", categoryName);
        }
        
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        return String.format("In the '%s' category, you've spent %.2f TND across %d expenses.", 
            categoryName, total, expenses.size());
    } catch (Exception e) {
        return "Sorry, I couldn't filter your expenses. Please try again.";
    }
}
private String handleDeleteExpense(int userId, String message) {
    try {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        // Extract amount, category, and date from the message
        double amount = extractAmount(message);
        String categoryName = extractCategory(message);
        LocalDate date = extractDate(message);
        
        if (amount <= 0) {
            return "Please specify the amount of the expense you want to delete.";
        }
        
        if ("miscellaneous".equals(categoryName)) {
            return "Please specify the category of the expense you want to delete.";
        }

        Category category = categoryRepository.findByNameAndUser(categoryName, user);
        if (category == null) {
            return String.format("You don't have any expenses in the '%s' category.", categoryName);
        }

        // Find matching expenses - prioritize by date if specified, otherwise by most recent
        List<Expense> matchingExpenses;
        if (message.toLowerCase().contains("today") || message.toLowerCase().contains("yesterday")) {
            matchingExpenses = expenseRepository.findByUserAndCategoryAndDate(user, category, date);
        } else {
            matchingExpenses = expenseRepository.findByUserAndCategoryOrderByDateDesc(user, category);
        }
        
        Expense expenseToDelete = matchingExpenses.stream()
            .filter(expense -> Math.abs(expense.getAmount() - amount) < 0.01)
            .findFirst()
            .orElse(null);

        if (expenseToDelete == null) {
            return String.format("I couldn't find an expense of %.2f TND in the '%s' category%s.", 
                amount, categoryName, 
                message.toLowerCase().contains("today") || message.toLowerCase().contains("yesterday") 
                    ? " for that date" : "");
        }

        // Store details before deletion
        float deletedAmount = expenseToDelete.getAmount();
        String deletedCategory = expenseToDelete.getCategory().getName();
        LocalDate deletedDate = expenseToDelete.getDate();
        
        expenseRepository.delete(expenseToDelete);
        
        return String.format("Deleted expense: %.2f TND for %s on %s.", 
            deletedAmount, deletedCategory, deletedDate);
        
    } catch (Exception e) {
        e.printStackTrace();
        return "Sorry, I couldn't delete the expense. Please try again.";
    }
}
}



