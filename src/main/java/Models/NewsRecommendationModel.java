package Models;

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
        return executorService.submit(() -> {
            List<Article> recommendations = new ArrayList<>(); // To store recommended articles
            Map<String, Integer> termFrequency = new HashMap<>();
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

            // Filter and score articles for recommendations
            for (Article article : articles) {
                // Exclude articles already read, skipped, or not matching user preferences
                if (!user.getReadArticles().contains(article.getId()) && // Exclude read articles
                        !skippedArticles.contains(article.getId()) &&  // Exclude skipped articles
                        user.getPreferences().contains(article.getCategory()) &&  // Match preferences
                        recommendations.stream().noneMatch(a -> a.getId().equals(article.getId()))) {  // Avoid duplicates
                    RealVector articleVector = articleVectors.get(article);
                    double score = calculateAverageSimilarity(likedArticleVectors, articleVector); // Calculate similarity score
                    if (score > 0) {
                        recommendations.add(article); // Add article to recommendations if it has a positive score
                    }
                }
            }

            // If no preferences, recommend from "General" category
            if (recommendations.isEmpty()) {
                for (Article article : articles) {
                    if (!skippedArticles.contains(article.getId()) && // Exclude skipped articles
                            article.getCategory().equalsIgnoreCase("General")) {
                        recommendations.add(article);
                    }
                }
            }

            // Sort recommendations by similarity score, descending order
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

    // Helper method to fetch articles by IDs
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
}
