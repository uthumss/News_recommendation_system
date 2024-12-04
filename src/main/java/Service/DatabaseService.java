package Service;

import DatabaseManager.DatabaseManager;
import Templates.Article;
import Templates.User;

import java.util.Scanner;

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
}
