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

        while (true) {
            try {
                userManager.clearConsole();
                System.out.println("Enter Command ⬇️");
                System.out.println();
                System.out.println("\uD83D\uDD39 1 for Create Account");
                System.out.println("\uD83D\uDD39 2 for Login");
                System.out.println("\uD83D\uDD39 3 to Exit");
                System.out.print(">");

                int command = scanner.nextInt();
                scanner.nextLine();
                userManager.clearConsole();

                if (command == 1) {
                    userManager.createAccount(system, dbManager);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        System.out.println("‼️ Timer interrupted: " + e.getMessage());
                    }
                } else if (command == 2) {
                    userManager.login(system, dbManager, dbService);
                } else if (command == 3) {
                    System.out.println("\uD83D\uDED1 Exiting application...");
                    break;
                } else {
                    System.out.println("❗ Invalid command, try again.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e2){
                        System.out.println("‼️ Timer interrupted: " + e2.getMessage());
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("❗ Invalid input. Please enter a number");
                try {
                    Thread.sleep(2000);
                } catch (Exception e1){
                    System.out.println("‼️ Timer interrupted: " + e1.getMessage());
                }
                scanner.nextLine();
            }
        }
        scanner.close();
        system.shutdown();
    }

}
