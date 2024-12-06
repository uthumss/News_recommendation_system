package Templates;

import DatabaseManager.DatabaseManager;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<String> readArticles;
    private List<String> likedArticles;
    private List<String> skippedArticles;
    private List<String> preferences;
    protected DatabaseManager dbmanager;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.readArticles = new ArrayList<>();
        this.likedArticles = new ArrayList<>();
        this.skippedArticles = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    // Method to add a preferred category
    public void addPreferredCategory(String category) {
        preferences.add(category);
    }

    // Method to remove a preferred category
    public void removePreferredCategory(String category) {
        preferences.remove(category);
        dbmanager.deleteUserPreference(username,category);
    }

    // Method to get preferred category from database
    public List<String> getPreferences() {
        if (dbmanager == null) {
            throw new IllegalStateException("DatabaseManager is not initialized for this user.");
        }
        return dbmanager.getUserPreferences(this);
    }

    // Method to add an article to the read list
    public void addReadArticle(String username,String article) {
        readArticles.add(article);
        dbmanager.saveReadArticle(username, article);
    }

    // Method to add an article to the liked list
    public void addLikedArticle(String article) {
        likedArticles.add(article);
    }

    // Method to remove an article from the liked list
    public void removeLikedArticle(String username,String article) {
        likedArticles.remove(article);
        dbmanager.removeLikedArticle(username,article);
    }

    public void setSkippedArticles(List<String> skippedArticles) {
        this.skippedArticles = skippedArticles;
    }

    // Method to get skipped articles from the database
    public List<String> getSkippedArticles() {
        return dbmanager.getSkippedArticles(username);
    }

    // Method to get read articles from database
    public List<String> getReadArticles() {
        return dbmanager.getReadArticles(this.username);
    }

    // Method to get liked articles from the database
    public List<String> getLikedArticles() {
        return dbmanager.getLikedArticles(this.username);
    }

    // Method to set the database manager
    public void setDatabaseManager(DatabaseManager dbmanager) {
        this.dbmanager = dbmanager;
    }



    // Method to sync the user details to database
    public void syncToDatabase(DatabaseManager dbManager) {
        for (String category : this.preferences) {
            dbManager.saveUserPreference(this.username, category);
        }
        for (String articleId : this.likedArticles) {
            dbManager.saveLikedArticle(this.username, articleId);
        }
        for (String articleId : this.readArticles) {
            dbManager.saveReadArticle(this.username, articleId);
        }
    }


}
