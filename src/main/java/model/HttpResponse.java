package model;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final HttpStatus status;
    private byte[] body = new byte[0];
    private String contentType = "text/plain;charset=utf-8";
    private final Map<String, String> headers = new HashMap<>();

    private HttpResponse(HttpStatus status){
        this.status = status;
    }

    public static HttpResponse of(HttpStatus status){
        if (status == null) {
            throw new IllegalArgumentException("HttpStatus must not be null");
        }

        return new HttpResponse(status);
    }

    public static HttpResponse redirect(String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Redirect location must not be empty");
        }
        return HttpResponse.of(HttpStatus.SEE_OTHER)
                .header("Location", location);
    }

    public HttpResponse header(String key, String value){
        if(key != null && value != null)
            headers.put(key, value);
        return this;
    }

    public HttpResponse body(byte[] body){
        if(body != null){
            this.body = body;
        }
        return this;
    }

    public HttpResponse contentType(String contentType) {
        if (contentType != null) {
            this.contentType = contentType;
        }
        return this;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public byte[] getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
