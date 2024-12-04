package Driver;

import java.net.URI;
import java.util.*;
import java.awt.Desktop;

import DatabaseManager.DatabaseManager;
import Models.ArticleClassifier;
import Models.NewsFetcher;
import Models.NewsRecommendationModel;
import Service.DatabaseService;
import Service.UserManagement;
import Templates.Admin;
import Templates.Article;
import Templates.User;


public class NewsRecoDriver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        NewsFetcher newsFetcher = new NewsFetcher();
        DatabaseManager dbManager = new DatabaseManager();
        DatabaseService dbService = new DatabaseService();
        NewsRecommendationModel system = new NewsRecommendationModel(4);
        UserManagement userManager = new UserManagement();

        // Load all articles from database
        dbService.loadArticlesFromDB(dbManager, system);

        while (true) {
            try {
                clearConsole();
                System.out.println("Enter Command ⬇️");
                System.out.println();
                System.out.println("\uD83D\uDD39 1 for Create Account");
                System.out.println("\uD83D\uDD39 2 for Login");
                System.out.println("\uD83D\uDD39 3 to Exit");
                System.out.print(">");

                int command = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                clearConsole();

                if (command == 1) {
                    dbService.createAccount(system, dbManager, scanner);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println("‼️ Timer interrupted: " + e.getMessage());
                    }
                } else if (command == 2) {
                    login(system, dbManager, scanner, newsFetcher, dbService, userManager);
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





    private static void login(NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner, NewsFetcher newsFetcher, DatabaseService dbService, UserManagement userManager) {
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

        userMenu(user, system, dbManager, scanner,dbService, userManager);
    }




    private static void adminMenu(Admin admin, NewsRecommendationModel system, Scanner scanner, NewsFetcher newsFetcher, DatabaseManager dbManager) {
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



    private static void userMenu(User user, NewsRecommendationModel system, DatabaseManager dbManager, Scanner scanner,DatabaseService dbService, UserManagement userManager) {
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
                system.getRecommendations(user, dbManager, scanner,dbService,userManager);
            } else if (choice == 2) {
                userManager.manageProfile(user, dbManager, scanner);
            } else if (choice == 3) {
                System.out.println("Logging out...");
                break;
            } else {
                System.out.println("❗ Invalid option");
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
