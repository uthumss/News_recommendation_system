package Driver;

import java.util.*;

import DatabaseManager.DatabaseManager;
import Models.NewsRecommendationModel;
import Service.DatabaseService;
import Service.UserManagement;


public class NewsRecoDriver {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DatabaseManager dbManager = new DatabaseManager();
        DatabaseService dbService = new DatabaseService();
        NewsRecommendationModel system = new NewsRecommendationModel(4);
        UserManagement userManager = new UserManagement();

        // Load all articles from database
        dbService.loadArticlesFromDB(dbManager, system);

        // Main application loop
        while (true) {
            try {
                // Clear the console for a clean UI
                userManager.clearConsole();

                // Display main menu options
                System.out.println("Enter Command ⬇️");
                System.out.println();
                System.out.println("\uD83D\uDD39 1 for Create Account");
                System.out.println("\uD83D\uDD39 2 for Login");
                System.out.println("\uD83D\uDD39 3 to Exit");
                System.out.print(">");

                int command = scanner.nextInt();
                scanner.nextLine();
                userManager.clearConsole();

                if (command == 1) { // To Create account
                    // Handle account creation
                    userManager.createAccount(system, dbManager);
                    userManager.timer(3000);
                } else if (command == 2) { // To login
                    userManager.login(system, dbManager, dbService);
                } else if (command == 3) { // Exit program
                    System.out.println("\uD83D\uDED1 Exiting application...");
                    break;
                } else { // Invalid
                    System.out.println("❗ Invalid command, try again.");
                    userManager.timer(2000);
                }
            } catch (InputMismatchException e) {
                System.out.println("❗ Invalid input. Please enter a number");
                userManager.timer(2000);
                scanner.nextLine();
            }
        }
        scanner.close();
        system.shutdown();
    }

}
