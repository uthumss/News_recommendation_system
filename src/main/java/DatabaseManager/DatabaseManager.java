package DatabaseManager;

import Templates.Admin;
import Templates.Article;
import Templates.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:news.db");
    }

    // Save user to the database
    public void saveUser(String username, String password, String role) {
        String sql = "INSERT OR IGNORE INTO users(username, password, role) VALUES(?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    // Load all users from the database
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, password FROM users WHERE role = 'user'";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");

                User user = new User(username, password);
                user.setDatabaseManager(this);  // Set the DatabaseManager for User
                users.add(user);  // Add the user to the users list

            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    // Authenticate user
    public User authenticateUser(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                if (role.equalsIgnoreCase("admin")) {
                    Admin admin = new Admin(username, password);
                    admin.setDatabaseManager(this); // Set DatabaseManager for Admin
                    return admin;
                } else {
                    User user = new User(username, password);
                    user.setDatabaseManager(this); // Set DatabaseManager for User
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
        return null; // If invalid credentials
    }

    // Save article to database
    public void saveArticle(Article article) {
        String sql = "INSERT INTO articles(headline, description, link, category) VALUES(?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getDescription() != null ? article.getDescription() : "No Description");
            pstmt.setString(3, article.getLink());
            pstmt.setString(4, article.getCategory());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving article: " + e.getMessage());
        }
    }

    // Load all articles from database
    public List<Article> loadArticles() {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT article_id, headline, description, link, category FROM articles";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("article_id");
                String title = rs.getString("headline");
                String description = rs.getString("description");
                String link = rs.getString("link");
                String category = rs.getString("category");

                Article article = new Article(id, title, description, link);
                article.setCategory(category);
                articles.add(article);
            }
        } catch (SQLException e) {
            System.err.println("Error loading articles from database: " + e.getMessage());
        }
        return articles;
    }

    // Save User Preference
    public void saveUserPreference(String username, String category) {
        String sql = "INSERT OR IGNORE INTO user_preferences(username, category) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving preference: " + e.getMessage());
        }
    }

    // Retrieve preferences of the relevant user from the database
    public List<String> getUserPreferences(User user) {
        List<String> preferences = new ArrayList<>();
        String sql = "SELECT category FROM user_preferences WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                preferences.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching preferences for user " + user.getUsername() + ": " + e.getMessage());
        }
        user.setPreferences(preferences);
        return preferences;
    }

    // Save Liked article to the database
    public void saveLikedArticle(String username, String articleId) {
        String sql = "INSERT OR IGNORE INTO user_liked_articles(username, article_id) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving liked article: " + e.getMessage());
        }
    }

    // Retrieve liked articles of the relevant user from the database
    public List<String> getLikedArticles(String username) {
        List<String> likedArticles = new ArrayList<>();
        String sql = "SELECT article_id FROM user_liked_articles WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                likedArticles.add(rs.getString("article_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching liked articles: " + e.getMessage());
        }
        return likedArticles;
    }

    // Save read articles to database
    public void saveReadArticle(String username, String articleId) {
        String sql = "INSERT OR IGNORE INTO user_read_articles(username, article_id) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving read article: " + e.getMessage());
        }
    }

    // Retrieve read articles from the database
    public List<String> getReadArticles(String username) {
        List<String> readArticles = new ArrayList<>();
        String sql = "SELECT article_id FROM user_read_articles WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                readArticles.add(rs.getString("article_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching read articles: " + e.getMessage());
        }
        return readArticles;
    }

    // Save skipped article to database
    public void saveSkippedArticle(String username, String articleId) {
        String sql = "INSERT OR IGNORE INTO user_skipped_articles(username, article_id) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving skipped article: " + e.getMessage());
        }
    }

    // Retrieve skipped articles from database
    public List<String> getSkippedArticles(String username) {
        List<String> skippedArticles = new ArrayList<>();
        String sql = "SELECT article_id FROM user_skipped_articles WHERE username = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                skippedArticles.add(rs.getString("article_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching skipped articles: " + e.getMessage());
        }
        return skippedArticles;
    }

    // To remove duplicates from the database after fetching articles from API
    public void removeDuplicateArticles() {
        String sql = """
        DELETE FROM articles
        WHERE rowid NOT IN (
            SELECT MIN(rowid)
            FROM articles
            GROUP BY headline
        );
    """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            System.out.println(rowsAffected + " duplicate articles removed.");
        } catch (SQLException e) {
            System.err.println("Error removing duplicate articles: " + e.getMessage());
        }
    }

}
