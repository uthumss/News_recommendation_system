public class Article {
    private static int idCounter = 1;
    private String id;
    private String title;
    private String description;
    private String link;
    private String body;
    private String category;

    public Article(String title, String description, String link) {
        this.id = "A" + (idCounter++);
        this.title = title;
        this.description = description;
        this.link = link;
    }
}
