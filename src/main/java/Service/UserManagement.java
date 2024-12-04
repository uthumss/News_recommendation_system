package Service;

import DatabaseManager.DatabaseManager;
import Templates.Article;
import Templates.User;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UserManagement {

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "technology", "health", "sports", "business", "politics", "entertainment", "education", "lifestyle", "weather", "general");



    public static void manageProfile(User user, DatabaseManager dbManager, Scanner scanner) {
        while (true) {
            System.out.println("Manage Profile:");
            System.out.println("1 - Add Preferred Category");
            System.out.println("2 - Remove Preferred Category");
            System.out.println("3 - View Liked Articles");
            System.out.println("4 - Back to User Menu");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            clearConsole();

            if (choice == 1) {
                // Show the user valid categories and their current preferences
                System.out.println("Your Current Preferences: " + user.getPreferences());

                if (user.getPreferences().size() >= 3) {
                    System.out.println("You already have the maximum number of preferred categories.");
                    continue;
                }

                System.out.println("Valid Categories: " + VALID_CATEGORIES);
                System.out.println("You can only add up to 3 categories.");

                System.out.println("Enter categories to add (comma-separated if multiple):");
                String input = scanner.nextLine().trim().toLowerCase();
                clearConsole();
                String[] categoriesToAdd = input.split(",");

                for (String category : categoriesToAdd) {
                    category = category.trim();
                    if (!VALID_CATEGORIES.contains(category)) {
                        System.out.println("Invalid category: " + category);
                    } else if (user.getPreferences().contains(category)) {
                        System.out.println("Category already added: " + category);
                    } else if (user.getPreferences().size() >= 3) {
                        System.out.println("Cannot add more categories. Limit reached.");
                        break;
                    } else {
                        user.addPreferredCategory(category);
                        System.out.println("Added category: " + category);
                        user.syncToDatabase(dbManager); // Sync changes
                    }
                }


            } else if (choice == 2) {
                // Show the user's current preferences
                System.out.println("Your Current Preferences: " + user.getPreferences());
                if (user.getPreferences().isEmpty()) {
                    System.out.println("You have no preferences to remove.");
                    continue;
                }

                System.out.println("Enter category to remove:");
                String category = scanner.nextLine().trim().toLowerCase();
                if (!user.getPreferences().contains(category)) {
                    System.out.println("Category not found in your preferences: " + category);
                } else {
                    user.removePreferredCategory(category);
                    user.syncToDatabase(dbManager); // Sync changes
                }

            }  else if (choice == 3) {
                // Display liked articles
                List<Article> likedArticles = dbManager.viewLikedArticles(user.getUsername());
                if (likedArticles.isEmpty()) {
                    System.out.println("You have no liked articles.");
                } else {
                    System.out.println("Liked Articles:");
                    for (int i = 0; i < likedArticles.size(); i++) {
                        System.out.println((i + 1) + " - " + likedArticles.get(i).getTitle());
                    }
                    System.out.println("Enter the number of the article to open its link, or 0 to go back:");
                    int articleChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (articleChoice > 0 && articleChoice <= likedArticles.size()) {
                        Article selectedArticle = likedArticles.get(articleChoice - 1);
                        user.openLinkInBrowser(selectedArticle.getLink());
                    } else if (articleChoice == 0) {
                        System.out.println("Returning to Manage Profile...");
                    } else {
                        System.out.println("Invalid selection.");
                    }
                }

            } else if (choice == 4) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    // Method to print blank lines to simulate clearing the console
    private static void clearConsole() {
        for (int i = 0; i < 50; i++) { // Adjust the number as needed for your screen
            System.out.println();
        }
    }


}
