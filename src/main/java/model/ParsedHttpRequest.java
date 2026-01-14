package model;

import java.util.Collections;
import java.util.Map;

public final class ParsedHttpRequest {
    private final String path;
    private final Map<String, String> queryParameters;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final String body;
    private final byte[] rawBody;
    private final Map<String, String> cookies;

    public ParsedHttpRequest(HttpMethod method, String path, Map<String, String> queryParameters, 
                             Map<String, String> headers, Map<String, String> cookies, String body, byte[] rawBody) {
        this.path = path;
        this.queryParameters = Collections.unmodifiableMap(queryParameters);
        this.method = method;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
        this.rawBody = rawBody;
    }

    public String getPath() {
        return path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getHeader(String name) {
        if (headers == null) return null;
        return headers.get(name.toLowerCase());
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String getCookie(String name) {
        if (cookies == null) return null;
        return cookies.get(name);
    }

    public String getBody() {
        return body;
    }

    public byte[] getRawBody() {
        return rawBody;
    }
}
