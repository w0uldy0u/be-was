package model;

public final class ParsedHttpRequest {
    private final String path;
    private final HttpMethod method;
    private final String header;
    private final String body;

    public ParsedHttpRequest(HttpMethod method, String path, String header, String body){
        this.path = path;
        this.method = method;
        this.header = header;
        this.body = body;
    }

    public String getPath(){
        return path;
    }

    public HttpMethod getMethod(){
        return method;
    }

    public String getHeader(){
        return header;
    }

    public String getBody(){
        return body;
    }
}
