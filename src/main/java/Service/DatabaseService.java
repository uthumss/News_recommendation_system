package Service;

import DatabaseManager.DatabaseManager;
import Models.NewsRecommendationModel;
import Templates.Article;
import Templates.User;

import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DatabaseService {

    public static void handleArticleInteraction(User user, Article article, DatabaseManager dbManager, Scanner scanner) {
        // Open the link in the default browser
        System.out.println("Opening article in browser...");
        user.openLinkInBrowser(article.getLink());

        while (true) {
            try {
                System.out.println("Options ⬇\uFE0F");
                System.out.println("❤\uFE0F 1-Like");
                System.out.println("↩\uFE0F 2-Return to Recommendations");
                System.out.print("> ");
                int action = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (action == 1) {
                    user.addLikedArticle(article.getId());
                    user.syncToDatabase(dbManager); // Sync changes
                    System.out.println("You liked this article.");
                    break; // Exit after liking the article
                } else if (action == 2) {
                    dbManager.saveReadArticle(user.getUsername(), article.getId()); // Save to DB
                    user.addReadArticle(article.getId()); // Update in memory
                    System.out.println("Returning to recommendations...");
                    break; // Exit to return to recommendations
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

    // Method to load all articles in the database to the system
    public void loadArticlesFromDB(DatabaseManager dbManager, NewsRecommendationModel system) {
        try {
            List<Article> articles = dbManager.loadArticles();

            // Ensure no duplicate articles are added to the system
            Set<String> seenTitles = new HashSet<>();
            for (Article article : articles) {
                if (!seenTitles.contains(article.getTitle())) {
                    system.addArticle(article);
                    seenTitles.add(article.getTitle());
                }
            }

            System.out.println("Articles loaded successfully into the system.");
        } catch (Exception e) {
            System.err.println("Error loading articles: " + e.getMessage());
        }
    }

    // Method to create a user account
    public void createAccount(NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner) {
        while (true) {
            System.out.print("\uD83D\uDD37 Enter username: ");
            String username = scanner.nextLine();

            // Check if the username already exists
            if (dbManager.isUsernameTaken(username)) {
                System.out.println("❗Username already exists. Please choose a different username.");
                continue;
            }

            System.out.print("\uD83D\uDD37 Enter password: ");
            String password = scanner.nextLine();

            User user = new User(username, password);
            user.setDatabaseManager(dbManager); // Set DatabaseManager for User
            system.addUser(user);
            dbManager.saveUser(username, password, "user");
            System.out.println("✅ User account created");
            break;
        }
    }

}
