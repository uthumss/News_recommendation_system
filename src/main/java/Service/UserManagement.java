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
    private Scanner scanner = new Scanner(System.in);
    private NewsFetcher newsFetcher = new NewsFetcher();

    // Method to call when user chooses to manage profile from the user menu
    public void manageProfile(User user, DatabaseManager dbManager) {
        while (true) {
            clearConsole();
            System.out.println("Manage Profile:");
            System.out.println("1 - Add Preferred Category");
            System.out.println("2 - Remove Preferred Category");
            System.out.println("3 - View Liked Articles");
            System.out.println("4 - Back to User Menu");
            System.out.print("➡️ Enter your choice: ");
//            int choice = scanner.nextInt();
//            scanner.nextLine(); // Consume newline

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (choice == 1) {
                    clearConsole();
                    // Show the user valid categories and their current preferences
                    System.out.println("\uD83D\uDCC3 Your Current Preferences: " + user.getPreferences());

                    if (user.getPreferences().size() >= 3) {
                        System.out.println("You already have the maximum number of preferred categories.");
                        timer(2000);
                        continue;
                    }

                    System.out.println("\uD83D\uDCDC Valid Categories: " + newsFetcher.classifier.CATEGORY_KEYWORDS.keySet());
                    System.out.println("You can only add up to 3 categories.");
                    System.out.println();


                    System.out.print("➡️ Enter categories to add (comma-separated if multiple):");
                    String input = scanner.nextLine().trim().toLowerCase();
                    String[] categoriesToAdd = input.split(",");

                    for (String category : categoriesToAdd) {
                        category = category.trim();
                        if (!newsFetcher.classifier.CATEGORY_KEYWORDS.keySet().contains(category)) {
                            System.out.println("❗Invalid category: " + category);
                            timer(2000);
                        } else if (user.getPreferences().contains(category)) {
                            System.out.println("❌ Category already added: " + category);
                            timer(2000);
                        } else if (user.getPreferences().size() >= 3) {
                            System.out.println("❗Cannot add more categories. Limit reached.");
                            timer(2000);
                            break;
                        } else {
                            user.addPreferredCategory(category);
                            System.out.println("Added category: " + category);
                            user.syncToDatabase(dbManager); // Sync changes
                        }
                    }


                } else if (choice == 2) {
                    clearConsole();
                    // Show the user's current preferences
                    System.out.println("\uD83D\uDCC3 Your Current Preferences: " + user.getPreferences());
                    if (user.getPreferences().isEmpty()) {
                        System.out.println("❌ You have no preferences to remove.");
                        timer(2000);
                        continue;
                    }

                    System.out.print("➡️ Enter category to remove:");
                    String category = scanner.nextLine().trim().toLowerCase();
                    if (!user.getPreferences().contains(category)) {
                        System.out.println("❌ Category not found in your preferences: " + category);
                        timer(3000);
                    } else {
                        user.removePreferredCategory(category);
                        user.syncToDatabase(dbManager); // Sync changes
                    }

                }  else if (choice == 3) {
                    // Display liked articles
                    List<Article> likedArticles = dbManager.viewLikedArticles(user.getUsername());
                    if (likedArticles.isEmpty()) {
                        System.out.println("❌ You have no liked articles.");
                        timer(3000);
                    } else {
                        clearConsole();
                        System.out.println("Liked Articles:");
                        for (int i = 0; i < likedArticles.size(); i++) {
                            System.out.println((i + 1) + " - " + likedArticles.get(i).getTitle());
                        }
                        System.out.print("➡️Enter the number of the article to open its link, or 0 to go back:");
                        if (scanner.hasNextInt()) {
                            int articleChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            if (articleChoice > 0 && articleChoice <= likedArticles.size()) {
                                Article selectedArticle = likedArticles.get(articleChoice - 1);
                                user.openLinkInBrowser(selectedArticle.getLink());
                            } else if (articleChoice == 0) {
                                System.out.println("Returning to Manage Profile...");
                                timer(2000);
                            } else {
                                System.out.println("❗Invalid selection");
                                timer(2000);
                            }
                        }else {
                            System.out.println("❗ Invalid input. Please enter a number.");
                            scanner.nextLine(); // Clear the invalid input
                            timer(2000);
                        }
                    }

                } else if (choice == 4) {
                    break;
                } else {
                    System.out.println("❗Invalid option");
                    timer(2000);
                }
            } else {
                System.out.println("❗ Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
                timer(2000);
            }


        }
    }

    // Method to create a user account
    public void createAccount(NewsRecommendationModel system, DatabaseManager dbManager) {
        while (true) {
            System.out.print("\uD83D\uDD37 Enter username: ");
            String username = scanner.nextLine();

            // Check if the username already exists
            if (dbManager.isUsernameTaken(username.trim())) {
                System.out.println("❗Username already exists. Please choose a different username.");
                continue;
            }

            // Validate username length and spaces
            if (username.trim().contains(" ") || username.trim().length() > 15) {
                System.out.println("❗Username cannot contain spaces and must be 15 characters or fewer.");
                continue;
            }

            System.out.print("\uD83D\uDD37 Enter password: ");
            String password = scanner.nextLine();

            // Validate password length and spaces
            if (password.trim().contains(" ") || password.trim().length() > 15) {
                System.out.println("❗Password cannot contain spaces and must be 15 characters or fewer.");
                continue;
            }

            if(!username.isEmpty() && !password.isEmpty()){
                User user = new User(username, password);
                user.setDatabaseManager(dbManager); // Set DatabaseManager for User
                system.addUser(user);
                dbManager.saveUser(username, password, "user");
                System.out.println("✅ User account created");
                break;
            }
            else{
                System.out.println("❌ Do not leave any field empty");
                break;
            }


        }
    }

    // Method for a user or admin to login to the relevant menu
    public void login(NewsRecommendationModel system, DatabaseManager dbManager, DatabaseService dbService) {
        System.out.print("\uD83D\uDD37 Enter username: ");
        String username = scanner.nextLine();
        System.out.print("\uD83D\uDD37 Enter password: ");
        String password = scanner.nextLine();


        // Check for predefined admins
        if (("admin1".equals(username) && "123".equals(password)) ||
                ("admin2".equals(username) && "456".equals(password))) {
            Admin admin = new Admin(username, password);
            admin.setDatabaseManager(dbManager);

            // Add admin to the system if not already loaded
            if (system.getUsers().stream().noneMatch(u -> u.getUsername().equals(username))) {
                system.addUser(admin);
            }
            adminMenu(admin, system, dbManager);
            return;
        }

        // Authenticate regular users
        User user = dbManager.authenticateUser(username, password);
        if (user == null) {
            System.out.println("❗ Invalid login credentials.");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("‼️ Timer interrupted: " + e.getMessage());
            }
            return;
        }

        // Add user to system if not already loaded
        if (system.getUsers().stream().noneMatch(u -> u.getUsername().equals(username))) {
            system.addUser(user);
        }

        userMenu(user, system, dbManager,dbService);
    }


    // Admin menu method
    public void adminMenu(Admin admin, NewsRecommendationModel system, DatabaseManager dbManager) {
        while (true) {
            clearConsole();
            System.out.println("Welcome " + admin.getUsername() + "!");
            System.out.println();
            System.out.println("Admin Menu ⬇️");
            System.out.println("\uD83D\uDD39 1 - Delete User");
            System.out.println("\uD83D\uDD39 2 - Remove Article");
            System.out.println("\uD83D\uDD39 3 - Fetch More Articles");
            System.out.println("\uD83D\uDD39 4 - Logout");
            System.out.print(">");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (choice == 1) {
                    List<User> users = dbManager.getAllUsers();
                    if (users.isEmpty()) {
                        System.out.println("❌ No users found.");
                        timer(2000);
                    } else {
                        clearConsole();
                        System.out.println("♟ All Registered Users");
                        System.out.println();
                        for (User user : users) {
                            System.out.println(" - " + user.getUsername());
                        }

                        // Prompt admin to enter a username for deletion
                        System.out.print("➡️ Enter username to delete:");
                        String username = scanner.nextLine();
                        admin.removeUser(users, username);
                        timer(2000);
                    }
                } else if (choice == 2) {
                    clearConsole();

                    // Get total number of articles
                    int totalArticles = dbManager.getArticleCount();
                    if (totalArticles == 0) {
                        System.out.println("❌ No articles found in the database.");
                        timer(2000);
                        continue;
                    }

                    System.out.println("\uD83D\uDCF0 Total number of articles: " + totalArticles);
                    System.out.print("➡️ Enter article ID to delete:");
                    String articleId = scanner.nextLine();

                    // Search for article by ID
                    Article article = findArticleById(system.getArticles(), articleId);
                    if (article != null) {
                        clearConsole();
                        System.out.println("❎ Found article: " + article.getTitle());
                        System.out.print("❗Are you sure you want to delete this article? (yes/no): ");
                        String confirmation = scanner.nextLine().trim().toLowerCase();

                        if (confirmation.equals("yes")) {
                            admin.deleteArticle(system.getArticles(), articleId);
                            timer(2000);
                        } else {
                            System.out.println("\uD83D\uDEAB Deletion cancelled.");
                            timer(2000);
                        }
                    } else {
                        System.out.println("❌ Article not found with ID: " + articleId);
                        timer(2000);
                    }
                } else if (choice == 3) {
                    clearConsole();
                    newsFetcher.loadInitialArticles(dbManager, system);
                    timer(3000);
                } else if (choice == 4) {
                    System.out.println("Logging out...");
                    break;
                } else {
                    System.out.println("❗Invalid option.");
                    timer(2000);
                }
            } else {
                System.out.println("❗ Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear the invalid input
                timer(2000);
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
    public void userMenu(User user, NewsRecommendationModel system, DatabaseManager dbManager,DatabaseService dbService) {
        while (true) {
            clearConsole();
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
                manageProfile(user, dbManager);
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

    //method for timer
    public void timer(int milisecs){
        try {
            Thread.sleep(milisecs);
        } catch (InterruptedException e) {
            System.out.println("‼️ Timer interrupted: " + e.getMessage());
        }
    }


}
