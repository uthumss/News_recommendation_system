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
    private UserManagement userManager = new UserManagement();
    private Scanner scanner = new Scanner(System.in);

    public void handleArticleInteraction(User user, Article article, DatabaseManager dbManager) {
        // Open the link in the default browser
        System.out.println("Opening article in browser...");
        user.openLinkInBrowser(article.getLink());

        while (true) {
            try {
                userManager.clearConsole();
                System.out.println("Options ⬇️ - " + article.getTitle());
                System.out.println("❤️ 1-Like");
                System.out.println("↩️ 2-Return to Recommendations");
                System.out.print("> ");
                int action = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (action == 1) {
                    user.addLikedArticle(article.getId());
                    user.syncToDatabase(dbManager); // Sync changes
                    System.out.println("You liked this article.");
                    Thread.sleep(2000);
                    userManager.clearConsole();

                    System.out.println("Options ⬇️ - " + article.getTitle());
                    System.out.println("\uD83D\uDC94 1-Dislike");
                    System.out.println("↩️ 2-Return to Recommendations");
                    System.out.print("> ");
                    int action1 = scanner.nextInt();
                    scanner.nextLine();

                    while(true) {
                        try {
                            if (action1 == 1) {
                                user.removeLikedArticle(user.getUsername(), article.getId());
                                System.out.println("You Disliked this article.");
                                Thread.sleep(2000);
                                break;
                            } else if (action1 == 2) {
                                System.out.println("Returning to recommendations...");
                                Thread.sleep(1000);
                                break; // Exit to return to recommendations
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid input. Please enter a valid number.");
                        }
                    }
                    break; // Exit after
                } else if (action == 2) {
                    user.addReadArticle(user.getUsername(),article.getId()); // Update in memory
                    System.out.println("Returning to recommendations...");
                    Thread.sleep(1000);
                    break; // Exit to return to recommendations
                } else {
                    System.out.println("Invalid choice. Please enter 1 or 2.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid number.");
                try {
                    Thread.sleep(2000);
                } catch (Exception e1){
                    System.out.println("‼️ Timer interrupted: " + e1.getMessage());
                }
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

}
