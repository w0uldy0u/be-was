package model;

public class Article {
    private final int id;
    private final String authorId;
    private final String content;
    private final String imagePath;
    private final int likes;

    public Article(int id, String authorId, String content, String imagePath, int likes) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.imagePath = imagePath;
        this.likes = likes;
    }

    public Article(int id, String authorId, String content, String imagePath) {
        this(id, authorId, content, imagePath, 0);
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

    public int getLikes() {
        return likes;
    }
}