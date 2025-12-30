package webserver.parser;

import model.HttpMethod;
import model.ParsedHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpParser {

    public ParsedHttpRequest parse(BufferedReader br) throws IOException {
        RequestLine rl = parseRequestLine(br);
        HeadersResult hr = parseHeaders(br);
        String body = parseBody(br, hr.contentLength);

        return new ParsedHttpRequest(rl.method, rl.path, hr.rawHeaders, body);
    }

    private RequestLine parseRequestLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null || line.isEmpty()) return null;

        String[] tokens = line.split(" ");
        if (tokens.length < 2) return null;

        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String path = tokens[1];
        return new RequestLine(method, path);
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
        RequestLine(HttpMethod method, String path) {
            this.method = method;
            this.path = path;
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