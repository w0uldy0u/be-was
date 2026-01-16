package webserver.parser;

import model.HttpMethod;
import model.ParsedHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {

    public ParsedHttpRequest parse(InputStream in) throws IOException {
        StringBuilder headerBuilder = new StringBuilder();
        int prev = 0;
        int curr;
        
        while ((curr = in.read()) != -1) {
            headerBuilder.append((char) curr);
            String built = headerBuilder.toString();
            if (built.endsWith("\r\n\r\n")) {
                break;
            }
        }

        String headerSection = headerBuilder.toString();
        String[] lines = headerSection.split("\r\n");
        
        if (lines.length == 0) {
            throw new IOException("Empty request");
        }

        RequestLine rl = parseRequestLine(lines[0]);
        
        HeadersResult hr = parseHeaders(lines);

        byte[] rawBody = new byte[0];
        String body = "";
        
        if (hr.contentLength > 0) {
            rawBody = new byte[hr.contentLength];
            int readTotal = 0;
            while (readTotal < hr.contentLength) {
                int r = in.read(rawBody, readTotal, hr.contentLength - readTotal);
                if (r == -1) break;
                readTotal += r;
            }
            body = new String(rawBody, 0, readTotal, StandardCharsets.UTF_8);
        }

        return new ParsedHttpRequest(rl.method, rl.path, rl.queryParameters, hr.headers, hr.cookies, body, rawBody);
    }

    private RequestLine parseRequestLine(String line) {
        if (line == null || line.isEmpty()) return null;

        String[] tokens = line.split(" ");
        if (tokens.length < 2) return null;

        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String[] pathAndQuery = tokens[1].split("\\?", 2);

        String path = pathAndQuery[0];
        Map<String, String> queryParams = parseQueryParams(pathAndQuery.length > 1 ? pathAndQuery[1] : null);

        return new RequestLine(method, path, queryParams);
    }

    public static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return queryParams;
        }

        String[] params = queryString.split("&");

        for (String param : params) {
            if (param.isEmpty()) continue;

            String[] kv = param.split("=", 2);
            String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";

            queryParams.put(key, value);
        }

        return queryParams;
    }

    private HeadersResult parseHeaders(String[] lines) {
        Map<String, String> headers = new HashMap<>();
        int contentLength = 0;
        Map<String, String> cookies = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) break;

            int idx = line.indexOf(':');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();

                headers.put(key.toLowerCase(), val);

                if (key.equalsIgnoreCase("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(val);
                        if (contentLength > 10 * 1024 * 1024) {
                            throw new IOException("Payload Too Large");
                        }
                    }
                    catch (NumberFormatException ignored) {}
                }
                else if(key.equalsIgnoreCase("Cookie")){
                    cookies = parseCookie(val);
                }
            }
        }

        return new HeadersResult(headers, contentLength, cookies);
    }

    private Map<String, String> parseCookie(String rawCookie) {
        Map<String, String> cookies = new HashMap<>();

        if (rawCookie == null || rawCookie.isEmpty()) {
            return cookies;
        }

        String[] pairs = rawCookie.split(";");

        for (String pair : pairs) {
            pair = pair.trim();
            if (pair.isEmpty()) continue;

            String[] kv = pair.split("=", 2);
            String key = kv[0].trim();
            String value = kv.length > 1 ? kv[1].trim() : "";

            if (!key.isEmpty()) {
                cookies.put(key, value);
            }
        }

        return cookies;
    }

    private static class RequestLine {
        final HttpMethod method;
        final String path;
        final Map<String, String> queryParameters;
        RequestLine(HttpMethod method, String path, Map<String, String> queryParameters) {
            this.method = method;
            this.path = path;
            this.queryParameters = queryParameters;
        }
    }

    private static class HeadersResult {
        final Map<String, String> headers;
        final int contentLength;
        final Map<String, String> cookies;
        HeadersResult(Map<String, String> headers, int contentLength, Map<String, String> cookies) {
            this.headers = headers;
            this.contentLength = contentLength;
            this.cookies = cookies;
        }
    }
}