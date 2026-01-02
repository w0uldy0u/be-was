package model;

import java.util.Collections;
import java.util.Map;

public final class ParsedHttpRequest {
    private final String path;
    private final Map<String, String> queryParameters;
    private final HttpMethod method;
    private final String header;
    private final String body;

    public ParsedHttpRequest(HttpMethod method, String path, Map<String, String> queryParameters, String header, String body){
        this.path = path;
        this.queryParameters = Collections.unmodifiableMap(queryParameters);
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

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getHeader(){
        return header;
    }

    public String getBody(){
        return body;
    }
}
