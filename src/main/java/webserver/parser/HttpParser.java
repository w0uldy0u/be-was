package webserver.parser;

import model.HttpMethod;
import model.ParsedHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpParser {

    public ParsedHttpRequest parse(BufferedReader br) throws IOException {
        RequestLine rl = parseRequestLine(br);
        HeadersResult hr = parseHeaders(br);
        String body = parseBody(br, hr.contentLength);

        return new ParsedHttpRequest(rl.method, rl.path, rl.queryParameters, hr.rawHeaders, body);
    }

    private RequestLine parseRequestLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null || line.isEmpty()) return null;

        String[] tokens = line.split(" ");
        if (tokens.length < 2) return null;

        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String[] pathAndQuery = tokens[1].split("\\?", 2);

        String path = pathAndQuery[0];
        Map<String, String> queryParams = parseQueryParams(pathAndQuery.length > 1 ? pathAndQuery[1] : null);

        return new RequestLine(method, path, queryParams);
    }

    private Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return queryParams;
        }

        String[] params = queryString.split("&");

        for (String param : params) {
            if (param.isEmpty()) continue;

            String[] kv = param.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";

            queryParams.put(key, value);
        }

        return queryParams;
    }

    private HeadersResult parseHeaders(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        int contentLength = 0;

        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            sb.append(line).append("\n");

            int idx = line.indexOf(':');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String val = line.substring(idx + 1).trim();
                if (key.equalsIgnoreCase("Content-Length")) {
                    try { contentLength = Integer.parseInt(val); }
                    catch (NumberFormatException ignored) {}
                }
            }
        }

        return new HeadersResult(sb.toString(), contentLength);
    }

    private String parseBody(BufferedReader br, int contentLength) throws IOException {
        if (contentLength <= 0) return "";

        char[] buf = new char[contentLength];
        int readTotal = 0;
        while (readTotal < contentLength) {
            int r = br.read(buf, readTotal, contentLength - readTotal);
            if (r == -1) break;
            readTotal += r;
        }
        return new String(buf, 0, readTotal);
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
        final String rawHeaders;
        final int contentLength;
        HeadersResult(String rawHeaders, int contentLength) {
            this.rawHeaders = rawHeaders;
            this.contentLength = contentLength;
        }
    }
}