package Service;

import DatabaseManager.DatabaseManager;
import Models.ArticleClassifier;
import Models.NewsFetcher;
import Models.NewsRecommendationModel;
import Templates.Admin;
import Templates.Article;
import Templates.User;

import java.util.List;
import java.util.Scanner;

public class UserManagement {
    private ArticleClassifier classifier = new ArticleClassifier();

    // Method to call when user chooses to manage profile from the user menu
    public void manageProfile(User user, DatabaseManager dbManager, Scanner scanner) {
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

                System.out.println("Valid Categories: " + classifier.CATEGORY_KEYWORDS.keySet());
                System.out.println("You can only add up to 3 categories.");

                System.out.println("Enter categories to add (comma-separated if multiple):");
                String input = scanner.nextLine().trim().toLowerCase();
                clearConsole();
                String[] categoriesToAdd = input.split(",");

                for (String category : categoriesToAdd) {
                    category = category.trim();
                    if (!classifier.CATEGORY_KEYWORDS.keySet().contains(category)) {
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

    // Method for a user or admin to login to the relevant menu
    public void login(NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner, NewsFetcher newsFetcher, DatabaseService dbService, UserManagement userManager) {
        System.out.print("\uD83D\uDD37 Enter username: ");
        String username = scanner.nextLine();
        System.out.print("\uD83D\uDD37 Enter password: ");
        String password = scanner.nextLine();
        clearConsole();

        // Check for predefined admins
        if (("admin1".equals(username) && "123".equals(password)) ||
                ("admin2".equals(username) && "456".equals(password))) {
            Admin admin = new Admin(username, password);
            admin.setDatabaseManager(dbManager);

            // Add admin to the system if not already loaded
            if (system.getUsers().stream().noneMatch(u -> u.getUsername().equals(username))) {
                system.addUser(admin);
            }
            adminMenu(admin, system, scanner, newsFetcher, dbManager);
            return;
        }

        // Authenticate regular users
        User user = dbManager.authenticateUser(username, password);
        if (user == null) {
            System.out.println("❗ Invalid login credentials.");
            return;
        }

        // Add user to system if not already loaded
        if (system.getUsers().stream().noneMatch(u -> u.getUsername().equals(username))) {
            system.addUser(user);
        }

        userMenu(user, system, dbManager, scanner,dbService);
    }


    // Admin menu method
    public void adminMenu(Admin admin, NewsRecommendationModel system, Scanner scanner, NewsFetcher newsFetcher, DatabaseManager dbManager) {
        while (true) {
            System.out.println("Welcome " + admin.getUsername() + "!");
            System.out.println();
            System.out.println("Admin Menu ⬇️");
            System.out.println("\uD83D\uDD39 1 - Delete User");
            System.out.println("\uD83D\uDD39 2 - Remove Article");
            System.out.println("\uD83D\uDD39 3 - Fetch More Articles");
            System.out.println("\uD83D\uDD39 4 - Logout");
            System.out.print(">");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1) {
                System.out.println("🧑 All Registered Users:");
                List<User> users = dbManager.getAllUsers();
                if (users.isEmpty()) {
                    System.out.println("No users found.");
                } else {
                    for (User user : users) {
                        System.out.println(" - " + user.getUsername());
                    }

                    // Prompt admin to enter a username for deletion
                    System.out.println("\uD83D\uDD37 Enter username to delete:");
                    String username = scanner.nextLine();
                    admin.removeUser(users, username);

//                    // Check if the username exists before attempting deletion
//                    boolean userFound = users.stream().anyMatch(u -> u.getUsername().equals(username));
//                    if (userFound) {
//                        admin.removeUser(users, username);
//                    } else {
//                        System.out.println("❌ User with username '" + username + "' not found.");
//                    }
                }
            } else if (choice == 2) {
                System.out.println("\uD83D\uDD37 Enter article ID to delete:");
                String articleId = scanner.nextLine();

                // Search for article by ID
                Article article = findArticleById(system.getArticles(), articleId);
                if (article != null) {
                    System.out.println("Found article: " + article.getTitle());
                    System.out.print("Are you sure you want to delete this article? (yes/no): ");
                    String confirmation = scanner.nextLine().trim().toLowerCase();

                    if (confirmation.equals("yes")) {
                        admin.deleteArticle(system.getArticles(), articleId);
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                } else {
                    System.out.println("❌ Article not found with ID: " + articleId);
                }
            } else if (choice == 3) {
                newsFetcher.loadInitialArticles(dbManager, system, scanner);
            } else if (choice == 4) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("❗Invalid option.");
            }
        }
    }

    // Helper method to find an article by ID
    private Article findArticleById(List<Article> articles, String articleId) {
        for (Article article : articles) {
            if (article.getId().equals(articleId)) {
                return article; // Return the matching article
            }
        }
        return null; // Return null if no article matches the given ID
    }

    // User menu method
    public void userMenu(User user, NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner,DatabaseService dbService) {
        while (true) {
            System.out.println("Welcome " + user.getUsername() + "!");
            System.out.println();
            System.out.println("User Menu ⬇\uFE0F");
            System.out.println("\uD83D\uDD39 1 - Get Recommendations");
            System.out.println("\uD83D\uDD39 2 - Manage Profile");
            System.out.println("\uD83D\uDD39 3 - Logout");
            System.out.print(">");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            clearConsole();

            if (choice == 1) {
                system.getRecommendations(user, dbManager, scanner,dbService,this);
            } else if (choice == 2) {
                manageProfile(user, dbManager, scanner);
            } else if (choice == 3) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("❗ Invalid option");
            }
        }
    }

    // Method to print blank lines to simulate clearing the console
    public void clearConsole() {
        for (int i = 0; i < 100; i++) { // Adjust the number as needed for your screen
            System.out.println();
        }
    }


}
