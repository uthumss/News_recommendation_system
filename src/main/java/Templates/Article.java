package Templates;

public class Article {
    private String id;
    private String title;
    private String description;
    private String link;
    private String category;

    public Article(String id, String title, String description, String link) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
