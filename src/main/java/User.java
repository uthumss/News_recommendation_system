import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private ArrayList<String> readArticles;
    private ArrayList<String> LikedArticles;
    private ArrayList<String> preferences;

    User(String username,String password){
        this.username = username;
        this.password = password;
        readArticles = new ArrayList<>();
        LikedArticles = new ArrayList<>();
        preferences = new ArrayList<>(3); // Because the user can only have 3 preferences max
    }
}
