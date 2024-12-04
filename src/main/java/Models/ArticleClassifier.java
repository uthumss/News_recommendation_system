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
    public static final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();

    static {
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



    private static Map<String, Integer> getStringIntegerMap(String description) {
        Map<String, Integer> categoryScores = new HashMap<>();
        String lowerCaseDescription = description.toLowerCase();

        // Calculate scores for each category by comparing with each word in the article description
        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            String[] keywords = entry.getValue();
            int score = 0;

            for (String keyword : keywords) {
                if (lowerCaseDescription.contains(keyword)) {
                    score++;
                }
            }

            categoryScores.put(category, score);
        }
        return categoryScores;
    }

    // Concurrently calculate scores for all categories
    private Map<String, Integer> getConcurrentCategoryScores(String description) {
        Map<String, Integer> categoryScores = new ConcurrentHashMap<>();
        List<Callable<Void>> tasks = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : CATEGORY_KEYWORDS.entrySet()) {
            String category = entry.getKey();
            String[] keywords = entry.getValue();

            tasks.add(() -> {
                int score = 0;
                String lowerCaseDescription = description.toLowerCase();
                for (String keyword : keywords) {
                    if (lowerCaseDescription.contains(keyword)) {
                        score++;
                    }
                }
                categoryScores.put(category, score);
                return null;
            });
        }

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            System.err.println("Error during concurrent classification: " + e.getMessage());
        }

        return categoryScores;
    }

    public void shutdown() {
        executorService.shutdown();
    }


}
