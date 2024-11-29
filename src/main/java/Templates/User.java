package Templates;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<String> readArticles;
    private List<String> likedArticles;
    private List<String> skippedArticles;
    private List<String> preferences;
    private DatabaseManager.DatabaseManager dbmanager;

    User(String username, String password) {
        this.username = username;
        this.password = password;
        this.readArticles = new ArrayList<>();
        this.likedArticles = new ArrayList<>();
        this.skippedArticles = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }
}
