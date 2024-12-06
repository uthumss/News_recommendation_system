package Models;

import Service.DatabaseService;
import Service.UserManagement;
import Templates.Article;
import Templates.User;
import DatabaseManager.DatabaseManager;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class NewsRecommendationModel {
    private List<User> users;
    private List<Article> articles;
    private ExecutorService executorService;
    private DatabaseManager dbManager = new DatabaseManager();

    public NewsRecommendationModel(int numThreads) {
        users = new ArrayList<>();
        articles = new ArrayList<>();
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    // Add user to the system
    public void addUser(User user) {
        users.add(user);
    }

    // Add article to the system
    public void addArticle(Article article) {
        articles.add(article);
    }

    // To recommend articles for a user based on their preferences and interactions with articles
    public Future<List<Article>> recommendArticles(User user) {
        // Use executorService to asynchronously compute recommendations
        return executorService.submit(() -> {
            // List to store recommended articles
            List<Article> recommendations = new ArrayList<>();

            // Map to store term frequencies for vocabulary building
            Map<String, Integer> termFrequency = new HashMap<>();

            // Set to store the vocabulary of unique terms across all articles
            Set<String> vocabulary = new HashSet<>();

            // Load user preferences and skipped articles from the database
            dbManager.getUserPreferences(user);
            List<String> skippedArticles = user.getSkippedArticles();

            // Build vocabulary and term frequencies for articles
            for (Article article : articles) {
                String[] terms = article.getDescription().toLowerCase().split("\\W+");
                for (String term : terms) {
                    vocabulary.add(term);
                    termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
                }
            }

            // Calculate TF-IDF for all articles
            Map<Article, RealVector> articleVectors = new HashMap<>();
            for (Article article : articles) {
                articleVectors.put(article, computeTFIDFVector(article, termFrequency, vocabulary));
            }

            // Get liked and read articles for the user
            List<Article> likedArticles = getArticlesByIds(user.getLikedArticles());
            List<Article> readArticles = getArticlesByIds(user.getReadArticles());

            // Convert liked articles to TF-IDF vectors
            List<RealVector> likedArticleVectors = new ArrayList<>();
            for (Article liked : likedArticles) {
                likedArticleVectors.add(articleVectors.get(liked));
            }

            // Convert read articles to TF-IDF vectors
            List<RealVector> readArticleVectors = new ArrayList<>();
            for (Article read : readArticles) {
                readArticleVectors.add(articleVectors.get(read));
            }

            // 1. Check for articles that belong to both the preferred category and liked articles
            for (Article article : articles) {
                if (skippedArticles.contains(article.getId())) {
                    continue;  // Skip articles already skipped
                }

                // Check if article belongs to the preferred category AND has similarity with liked articles
                if (user.getPreferences().contains(article.getCategory()) && likedArticles.contains(article)) {
                    RealVector articleVector = articleVectors.get(article);
                    double score = calculateAverageSimilarity(likedArticleVectors, articleVector);
                    if (score > 0) {
                        recommendations.add(article);  // Add article to recommendations if it has a positive score
                    }
                }
            }

            // 2. If no recommendations found, check for articles that match the preferred category
            if (recommendations.isEmpty()) {
                for (Article article : articles) {
                    if (skippedArticles.contains(article.getId())) {
                        continue;  // Skip articles already skipped
                    }

                    // Only recommend articles from preferred category
                    if (user.getPreferences().contains(article.getCategory())) {
                        recommendations.add(article);
                    }
                }
            }

            // 3. If no recommendations from "General", consider similarity with liked articles
            if (recommendations.isEmpty()) {
                for (Article article : articles) {
                    if (!skippedArticles.contains(article.getId()) && article.getCategory().equalsIgnoreCase("General")) {
                        // Compare the article to the liked articles
                        RealVector articleVector = articleVectors.get(article);
                        double score = calculateAverageSimilarity(likedArticleVectors, articleVector);
                        if (score > 0) {
                            recommendations.add(article);  // Add article to recommendations if it has a positive similarity score
                        }
                    }
                }
            }

            // Sort recommendations by similarity score (highest first)
            recommendations.sort(Comparator.comparingDouble(a -> -calculateAverageSimilarity(
                    likedArticles.stream().map(articleVectors::get).toList(),
                    articleVectors.get(a)
            )));

            return recommendations;
        });
    }

    // Calculate TF-IDF vector for an article
    private RealVector computeTFIDFVector(Article article, Map<String, Integer> termFrequency, Set<String> vocabulary) {
        Map<String, Integer> tf = new HashMap<>();
        String[] terms = article.getDescription().toLowerCase().split("\\W+");

        // Compute term frequency for the article
        for (String term : terms) {
            tf.put(term, tf.getOrDefault(term, 0) + 1);
        }

        // Build TF-IDF Vector
        double[] tfidf = new double[vocabulary.size()];
        int index = 0;
        for (String term : vocabulary) {
            double tfVal = tf.getOrDefault(term, 0);
            double idfVal = Math.log((double) articles.size() / (1 + termFrequency.getOrDefault(term, 0)));
            tfidf[index++] = tfVal * idfVal;
        }

        return new ArrayRealVector(tfidf);
    }

    // Calculate the average similarity between user liked articles and a candidate article
    private double calculateAverageSimilarity(List<RealVector> userVectors, RealVector articleVector) {
        if (userVectors.isEmpty()) return 0.0;
        double totalSimilarity = 0.0;
        for (RealVector userVector : userVectors) {
            totalSimilarity += cosineSimilarity(userVector, articleVector);
        }
        return totalSimilarity / userVectors.size();
    }

    // Calculate cosine similarity for 2 vectors
    private double cosineSimilarity(RealVector v1, RealVector v2) {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }

    // helper method to fetch articles by IDs
    private List<Article> getArticlesByIds(List<String> articleIds) {
        List<Article> result = new ArrayList<>();
        for (String id : articleIds) {
            for (Article article : articles) {
                if (article.getId().equals(id)) {
                    result.add(article);
                }
            }
        }
        return result;
    }

    // Sync articles from the database
    public void syncArticlesFromDatabase(DatabaseManager dbManager) {
        this.articles.clear(); // Clear existing articles
        this.articles.addAll(dbManager.loadArticles()); // Load fresh articles from the database
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void getRecommendations(User user, DatabaseManager dbManager, Scanner scanner, DatabaseService dbService, UserManagement userManager){
        dbManager.getUserPreferences(user); // Load preferences from DB
        List<String> skippedArticles = dbManager.getSkippedArticles(user.getUsername()); // Load skipped articles
        user.setSkippedArticles(skippedArticles); // Update the user's skipped articles

        if (user.getPreferences().isEmpty()) {
            System.out.println("❌ No preferences set. Please update your preferences first");
            userManager.timer(2000);
            userManager.manageProfile(user, dbManager);
            return;
        }

        try {
            List<Article> recommendations = this.recommendArticles(user).get();

            // Filter out skipped and read articles
            recommendations.removeIf(article ->
                    skippedArticles.contains(article.getId()) ||
                            user.getReadArticles().contains(article.getId())
            );

            if (recommendations.isEmpty()) {
                System.out.println("❌ No recommendations available");
                userManager.timer(2000);
                return;
            }

            int currentIndex = 0;
            int skippedCount = 0; // Track skipped articles
            while (currentIndex < recommendations.size()) {
                userManager.clearConsole();

                System.out.println("Recommendations ⬇️");

                // Show 3 recommendations at a time
                for (int i = 0; i < 3 && currentIndex + i < recommendations.size(); i++) {
                    Article article = recommendations.get(currentIndex + i);
                    System.out.println("\uD83D\uDD39 " + (i + 1) + " - " + article.getTitle());
                }

                System.out.println("\uD83D\uDD39 4 - See Other Recommendations");
                System.out.println("\uD83D\uDD39 5 - Back to Menu");
                System.out.print("➡️ Enter your choice: ");

                if (scanner.hasNextInt()) {
                    int action = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (action >= 1 && action <= 3) {
                        int selectedIndex = currentIndex + action - 1;
                        if (selectedIndex < recommendations.size()) {
                            Article selectedArticle = recommendations.get(selectedIndex);
                            dbService.handleArticleInteraction(user, selectedArticle, dbManager);
                        } else {
                            System.out.println("❗ Invalid selection. Try again.");
                            userManager.timer(2000);
                        }
                    } else if (action == 4) {
                        for (int i = 0; i < 3 && currentIndex + i < recommendations.size(); i++) {
                            skippedCount++;
                            Article skippedArticle = recommendations.get(currentIndex + i);
                            dbManager.saveSkippedArticle(user.getUsername(), skippedArticle.getId());
                        }
                        currentIndex += 3; // Move to next set
                    } else if (action == 5) {
                        System.out.println("Returning to menu...");
                        userManager.timer(1000);
                        break;
                    } else {
                        System.out.println("❗️ Invalid choice. Try again.");
                        userManager.timer(2000);
                    }

                    if (skippedCount >= 3) {
                        System.out.println("⏩ You skipped 3 articles. They will not be recommended again.");
                        skippedCount = 0; // Reset counter
                        userManager.timer(1000);
                    }
                } else {
                    System.out.println("❗ Invalid input. Please enter a number.");
                    scanner.nextLine(); // Clear the invalid input
                    userManager.timer(2000);
                }


            }

            if (currentIndex >= recommendations.size()) {
                System.out.println("No more recommendations available.");
                userManager.timer(2000);
            }
        } catch (Exception e) {
            System.err.println("Error fetching recommendations: " + e.getMessage());
        }
    }



}
