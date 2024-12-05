package Models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import DatabaseManager.DatabaseManager;
import Templates.Article;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class NewsFetcher {
    private static final String API_KEY = "4c6e1446eece454aa0c41c380842f9c3";
    private ArticleClassifier classifier = new ArticleClassifier();

    public List<Article> fetchArticles(String query) throws Exception {
        String urlString = "https://newsapi.org/v2/everything?q=" + query + "&apiKey=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Display fetching message
        System.out.println("Fetching Articles...");

        // To read the API response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine; // for a line of the response
        StringBuilder content = new StringBuilder(); // for full response

        // Looping through each line to append it to content
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        // Closing input stream
        in.close();

        // Disconnecting HTTP connection
        conn.disconnect();

        // Check for API limit exceeded response
        JSONObject jsonResponse = new JSONObject(content.toString());
        if (jsonResponse.has("status") && jsonResponse.getString("status").equals("error")) {
            String errorMessage = jsonResponse.optString("message", "An error occurred.");
            if (errorMessage.toLowerCase().contains("daily limit")) {
                System.out.println("Daily API key limit reached.");
            } else {
                System.out.println("Error: " + errorMessage);
            }
            return new ArrayList<>(); // Return an empty list in case of error
        }

        return parseArticles(content.toString());
    }


    // Method to parse articles from a JSON response string
    private List<Article> parseArticles(String jsonResponse) {
        List<Article> articles = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray articleArray = jsonObject.getJSONArray("articles");

        // A set to track articles by their ID or URL to avoid duplicates
        Set<String> fetchedArticleIds = new HashSet<>();
        Set<String> fetchedUrls = new HashSet<>();

        // Loop through each article in the "articles" JSON array
        for (int i = 0; i < articleArray.length(); i++) {
            JSONObject obj = articleArray.getJSONObject(i);

            // Extract required fields with default values in case they're missing
            String headline = obj.optString("title", "No Title");
            String description = obj.optString("description", "No Description");
            String link = obj.optString("url", "No Link");
            String articleId = obj.optString("id", link); // Use 'id' if available, else fallback to URL

            // Check if the article has been processed (by either ID or URL)
            if (!headline.equals("No Title") && !link.equals("No Link") && !description.equals("No Description")) {
                // Skip duplicate articles by ID or URL
                if (fetchedArticleIds.contains(articleId) || fetchedUrls.contains(link)) {
                    continue;
                }

                // Add the article to the sets to track duplicates
                fetchedArticleIds.add(articleId);
                fetchedUrls.add(link);

                // Create the article using the updated constructor
                Article article = new Article(articleId, headline, description, link);
                String category = classifier.classifyArticle(description);
                article.setCategory(category);
                articles.add(article);
            }
        }
        classifier.shutdown();

        return articles;
    }

    // method to load newly fetched articles to the DB and program
    public void loadInitialArticles(DatabaseManager dbManager, NewsRecommendationModel system, Scanner scanner) {
        try {
            System.out.println("Available categories: " + String.join(", ", classifier.CATEGORY_KEYWORDS.keySet()));
            System.out.print("Enter a category type to fetch articles: ");
            String query = scanner.nextLine().trim().toLowerCase();

            // Validate input
            if (query.isEmpty()) {
                System.out.println("Query cannot be empty. Please try again.");
                return;
            }

            String[] categories = query.split(",");
            for (String category : categories) {
                category = category.trim();
                if (!classifier.CATEGORY_KEYWORDS.keySet().contains(category)) {
                    System.out.println("Invalid category: " + category + ". Please enter valid categories.");
                    return;
                }
            }

            // Proceed with fetching articles
            List<Article> articles = this.fetchArticles(query);
            for (Article article : articles) {
                dbManager.saveArticle(article);
                system.addArticle(article);
            }
            // Remove duplicate articles after saving
            dbManager.removeDuplicateArticles();

            // Sync articles with the in-memory system
            system.syncArticlesFromDatabase(dbManager);

        } catch (Exception e) {
            System.err.println("Error loading initial articles: " + e.getMessage());
        }
    }

}
