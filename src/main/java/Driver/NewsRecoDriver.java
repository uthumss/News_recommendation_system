package Driver;

import java.net.URI;
import java.util.*;
import java.awt.Desktop;

import DatabaseManager.DatabaseManager;
import Models.ArticleClassifier;
import Models.NewsFetcher;
import Models.NewsRecommendationModel;
import Service.DatabaseService;
import Templates.Admin;
import Templates.Article;
import Templates.User;


public class NewsRecoDriver {
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "technology", "health", "sports", "business", "politics", "entertainment", "education", "lifestyle", "weather", "general");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        NewsFetcher newsFetcher = new NewsFetcher();
        ArticleClassifier classifier = new ArticleClassifier();
        DatabaseManager dbManager = new DatabaseManager();
        DatabaseService dbService = new DatabaseService();
        NewsRecommendationModel system = new NewsRecommendationModel(4);

        // Load all articles from database
        dbService.loadArticlesFromDB(dbManager, system);

        while (true) {
            try {
                clearConsole();
                System.out.println("Enter Command ⬇\uFE0F");
                System.out.println();
                System.out.println("\uD83D\uDD39 1 for Create Account");
                System.out.println("\uD83D\uDD39 2 for Login");
                System.out.println("\uD83D\uDD39 3 to Exit");
                System.out.print(">");

                int command = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                clearConsole();

                if (command == 1) {
                    createAccount(system, dbManager, scanner);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("‼\uFE0F Timer interrupted: " + e.getMessage());
                    }
                } else if (command == 2) {
                    login(system, dbManager, scanner, newsFetcher, classifier,dbService);
                } else if (command == 3) {
                    System.out.println("\uD83D\uDED1 Exiting application...");
                    break;
                } else {
                    System.out.println("❗ Invalid command, try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❗ Invalid input. Please enter a number");
                scanner.nextLine(); // Clear the invalid input
            }
        }
        scanner.close();
        system.shutdown();
    }


    private static void createAccount(NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner) {
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



    private static void login(NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner, NewsFetcher newsFetcher, ArticleClassifier classifier, DatabaseService dbService) {
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




    private static void adminMenu(Admin admin, NewsRecommendationModel system, Scanner scanner, NewsFetcher newsFetcher, DatabaseManager dbManager) {
        while (true) {
            System.out.println("Welcome " + admin.getUsername() + "!");
            System.out.println();
            System.out.println("Admin Menu ⬇\uFE0F");
            System.out.println("\uD83D\uDD39 1 - Delete User");
            System.out.println("\uD83D\uDD39 2 - Remove Article");
            System.out.println("\uD83D\uDD39 3 - Fetch More Articles");
            System.out.println("\uD83D\uDD39 4 - Logout");
            System.out.print(">");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 1) {
                System.out.println("\uD83D\uDD37 Enter username to delete:");
                String username = scanner.nextLine();
                admin.removeUser(system.getUsers(), username);
            } else if (choice == 2) {
                System.out.println("\uD83D\uDD37 Enter article ID to delete:");
                String articleId = scanner.nextLine();
                admin.deleteArticle(system.getArticles(), articleId);
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



    private static void userMenu(User user, NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner,DatabaseService dbService) {
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
                getRecommendations(user, system, dbManager, scanner,dbService);
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



    private static void getRecommendations(User user, NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner, DatabaseService dbService) {
        dbManager.getUserPreferences(user); // Load preferences from DB
        List<String> skippedArticles = dbManager.getSkippedArticles(user.getUsername()); // Load skipped articles
        user.setSkippedArticles(skippedArticles); // Update the user's skipped articles

        if (user.getPreferences().isEmpty()) {
            System.out.println("No preferences set. Please update your preferences first.");
            manageProfile(user, dbManager, scanner);
            return;
        }

        try {
            List<Article> recommendations = system.recommendArticles(user).get();

            // Filter out skipped and read articles
            recommendations.removeIf(article ->
                    skippedArticles.contains(article.getId()) ||
                            user.getReadArticles().contains(article.getId())
            );

            if (recommendations.isEmpty()) {
                System.out.println("No recommendations available based on your preferences.");
                return;
            }

            int currentIndex = 0;
            int skippedCount = 0; // Track skipped articles
            while (currentIndex < recommendations.size()) {
                System.out.println("Recommendations ⬇\uFE0F");

                // Show 3 recommendations at a time
                for (int i = 0; i < 3 && currentIndex + i < recommendations.size(); i++) {
                    Article article = recommendations.get(currentIndex + i);
                    System.out.println("\uD83D\uDD39 " + (i + 1) + " - " + article.getTitle());
                }

                System.out.println("\uD83D\uDD39 4 - See Other Recommendations");
                System.out.println("\uD83D\uDD39 5 - Back to Menu");
                System.out.print("➡\uFE0F Enter your choice: ");
                int action = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (action >= 1 && action <= 3) {
                    int selectedIndex = currentIndex + action - 1;
                    if (selectedIndex < recommendations.size()) {
                        Article selectedArticle = recommendations.get(selectedIndex);
                        dbService.handleArticleInteraction(user, selectedArticle, dbManager, scanner);
                    } else {
                        System.out.println("❗ Invalid selection. Try again.");
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
                    break;
                } else {
                    System.out.println("❗\uFE0F Invalid choice. Try again.");
                }

                if (skippedCount >= 3) {
                    System.out.println("You skipped 3 articles. They will not be recommended again.");
                    skippedCount = 0; // Reset counter
                }
            }

            if (currentIndex >= recommendations.size()) {
                System.out.println("No more recommendations available.");
            }
        } catch (Exception e) {
            System.err.println("Error fetching recommendations: " + e.getMessage());
        }
    }





    private static void manageProfile(User user, DatabaseManager dbManager, Scanner scanner) {
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
                    System.out.println("Removed category: " + category);
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
