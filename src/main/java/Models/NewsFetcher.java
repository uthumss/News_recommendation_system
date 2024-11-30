package Models;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import Templates.Article;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class NewsFetcher {
    private static final String API_KEY = "4c6e1446eece454aa0c41c380842f9c3";
    private final Set<String> fetchedUrls = new HashSet<>(); // To track fetched article URLs
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
//                article.setCategory("General"); // You can adjust category as needed
                String category = classifier.classifyArticle(description);
                article.setCategory(category);
                articles.add(article);
            }
        }

        return articles;
    }

}
