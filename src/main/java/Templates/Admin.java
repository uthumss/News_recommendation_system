package Templates;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User {
    public Admin(String username, String password) {
        super(username, password);
    }

    // Method to remove a user
    public void removeUser(List<User> users, String username) {
        List<User> usersToRemove = new ArrayList<>();

        // Iterate over the list to find users with the matching username
        for (User user : users) {
            if (user.getUsername().equals(username)){
                usersToRemove.add(user);
            }
        }

        // Remove all users in the usersToRemove list from the original users list
        for (User userToRemove : usersToRemove) {
            users.remove(userToRemove);
        }

        // Remove User from database
        if (dbmanager != null){
            dbmanager.deleteUserFromDatabase(username);
            System.out.println("✅ " + username + " has been removed from the system.");
        }
        else{
            System.out.println("‼\uFE0F Database not initialized.");
        }

    }

    // Method to delete an article
    public void deleteArticle(List<Article> articles, String articleId) {
        List<Article> articlesToRemove = new ArrayList<>();

        // Iterate over the list to find articles with the matching article Id
        for (Article article : articles) {
            if (article.getId().equals(articleId)){
                articlesToRemove.add(article);
            }
        }

        // Remove all articles in the articlesToRemove list from the original articles list
        for (Article articleToRemove : articlesToRemove){
            articles.remove(articleToRemove);
        }

    }

}
