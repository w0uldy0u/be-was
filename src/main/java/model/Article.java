package model;

public class Article {
    private final int id;
    private final String authorId;
    private final String content;

    public Article(int id, String authorId, String content) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
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
}