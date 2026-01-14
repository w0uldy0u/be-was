package model;

public class Article {
    private final int id;
    private final String authorId;
    private final String content;
    private final String imagePath;

    public Article(int id, String authorId, String content, String imagePath) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }

    public String getImagePath() {
        return imagePath;
    }
}