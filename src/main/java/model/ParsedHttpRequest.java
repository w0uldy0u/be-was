package model;

import java.util.Collections;
import java.util.Map;

public final class ParsedHttpRequest {
    private final String path;
    private final Map<String, String> queryParameters;
    private final HttpMethod method;
    private final String header;
    private final String body;
    private final Map<String, String> cookies;

    public ParsedHttpRequest(HttpMethod method, String path, Map<String, String> queryParameters, String header, Map<String, String> cookies, String body){
        this.path = path;
        this.queryParameters = Collections.unmodifiableMap(queryParameters);
        this.method = method;
        this.header = header;
        this.cookies = cookies;
        this.body = body;
    }

    public String getPath(){
        return path;
    }

    public HttpMethod getMethod(){
        return method;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getHeader(){
        return header;
    }

    public Map<String, String> getCookies(){
        return cookies;
    }

    public String getCookie(String name) {
        if (cookies == null) return null;
        return cookies.get(name);
    }

    public String getBody(){
        return body;
    }
}
