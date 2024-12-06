package Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ArticleClassifier {
    // ExecutorService for concurrent classification
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // Define keywords for each category
    public final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();
     {
        CATEGORY_KEYWORDS.put("business", new String[]{"investment", "capital", "startups", "business", "market", "company"});
        CATEGORY_KEYWORDS.put("sports", new String[]{"game", "team", "match", "player", "score", "sports"});
        CATEGORY_KEYWORDS.put("health", new String[]{"health", "coronavirus", "fitness", "disease", "ECG", "vaccine"});
        CATEGORY_KEYWORDS.put("technology", new String[]{"technology", "app", "robot", "software", "device", "AI", "innovation"});
        CATEGORY_KEYWORDS.put("politics", new String[]{"election", "government", "policy", "politics", "law", "politician"});
        CATEGORY_KEYWORDS.put("weather", new String[]{"weather", "storm", "forecast", "climate", "rain", "snow"});
        CATEGORY_KEYWORDS.put("lifestyle", new String[]{"fashion", "lifestyle", "culture", "home", "food", "living"});
        CATEGORY_KEYWORDS.put("entertainment", new String[]{"movie", "music", "celebrity", "show", "television", "performance"});
        CATEGORY_KEYWORDS.put("education", new String[]{"education", "school", "university", "learning", "classroom"});
    }

    // Classify the article based on the description
    public String classifyArticle(String description) {

        // Handling cases where the description is null or empty
        if (description == null || description.trim().isEmpty()) {
            return "Uncategorized";
        }

        // Setting up a score for each category to determine which category an article belongs to
        Map<String, Integer> categoryScores = getConcurrentCategoryScores(description);

        // Find the category with the highest score
        String bestCategory = "General";
        int highestScore = 0;

        for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }



    // Concurrently calculate scores for all categories
    private Map<String, Integer> getConcurrentCategoryScores(String description) {
        // Create a thread-safe map to store scores for each category
        Map<String, Integer> categoryScores = new ConcurrentHashMap<>();

        // Create a list of tasks to process each category's keywords concurrently
        List<Callable<Void>> tasks = new ArrayList<>();

        // loop over all categories and their associated keywords
        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            String category = entry.getKey(); // The current category name
            String[] keywords = entry.getValue(); // The keywords associated with this category

            // Create a task for the current category
            tasks.add(() -> {
                int score = 0; // Initialize the score for this category
                String lowerCaseDescription = description.toLowerCase();

                // Check each keyword to see if it appears in the description
                for (String keyword : keywords) {
                    if (lowerCaseDescription.contains(keyword)) {
                        score++; // Increment for each match
                    }
                }

                // Store the score in the thread-safe map
                categoryScores.put(category, score);
                return null;
            });
        }

        try {
            // Execute all the tasks in parallel using the executor service
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            // If something goes wrong during parallel execution
            System.err.println("Error during concurrent classification: " + e.getMessage());
        }

        // Return the map containing scores for each category
        return categoryScores;
    }

    public void shutdown() {
        executorService.shutdown();
    }


}
