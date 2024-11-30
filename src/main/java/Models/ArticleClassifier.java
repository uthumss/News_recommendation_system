package Models;

import java.util.HashMap;
import java.util.Map;

public class ArticleClassifier {
    // Define keywords for each category
    private static final Map<String, String[]> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("Business", new String[]{"investment", "capital", "startups", "business", "market", "company"});
        CATEGORY_KEYWORDS.put("Sports", new String[]{"game", "team", "match", "player", "score", "sports"});
        CATEGORY_KEYWORDS.put("Health", new String[]{"health", "coronavirus", "fitness", "disease", "ECG", "vaccine"});
        CATEGORY_KEYWORDS.put("Technology", new String[]{"technology", "app", "robot", "software", "device", "AI", "innovation"});
        CATEGORY_KEYWORDS.put("Social Media", new String[]{"social media", "TikTok", "Instagram", "YouTube", "Facebook", "social"});
        CATEGORY_KEYWORDS.put("Politics", new String[]{"election", "government", "policy", "politics", "law", "politician"});
        CATEGORY_KEYWORDS.put("Weather", new String[]{"weather", "storm", "forecast", "climate", "rain", "snow"});
        CATEGORY_KEYWORDS.put("Lifestyle", new String[]{"fashion", "lifestyle", "culture", "home", "food", "living"});
        CATEGORY_KEYWORDS.put("Entertainment", new String[]{"movie", "music", "celebrity", "show", "television", "performance"});
        CATEGORY_KEYWORDS.put("Education", new String[]{"education", "school", "university", "learning", "classroom"});
        CATEGORY_KEYWORDS.put("Crime", new String[]{"murder", "homicide", "crime", "robbery", "theft", "criminal"});
        CATEGORY_KEYWORDS.put("General", new String[]{"general", "miscellaneous", "other", "news", "update"});
    }

    // Classify the article based on the description
    public String classifyArticle(String description) {

        // Handling cases where the description is null or empty
        if (description == null || description.trim().isEmpty()) {
            return "Uncategorized";
        }

        // Setting up a score for each category to determine which category an article belongs to
        Map<String, Integer> categoryScores = getStringIntegerMap(description);

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
}
