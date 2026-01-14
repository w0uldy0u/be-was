package webserver.parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MultipartParser {

    public static class MultipartData {
        private final Map<String, String> textFields = new HashMap<>();
        private final Map<String, byte[]> fileFields = new HashMap<>();

        public void addTextField(String name, String value) {
            textFields.put(name, value);
        }

        public void addFileField(String name, byte[] data) {
            fileFields.put(name, data);
        }

        public String getTextField(String name) {
            return textFields.get(name);
        }

        public byte[] getFileField(String name) {
            return fileFields.get(name);
        }
    }

    public static MultipartData parse(byte[] body, String boundary) throws IOException {
        MultipartData result = new MultipartData();
        
        byte[] delimiterBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] finalDelimiterBytes = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);

        int pos = 0;
        
        pos = indexOf(body, delimiterBytes, pos);
        if (pos == -1) return result;
        pos += delimiterBytes.length;
        
        if (pos < body.length - 1 && body[pos] == '\r' && body[pos + 1] == '\n') {
            pos += 2;
        }

        while (pos < body.length) {
            int finalPos = indexOf(body, finalDelimiterBytes, pos - delimiterBytes.length - 2);
            if (finalPos != -1 && finalPos < pos) {
                break;
            }

            int headerEnd = findDoubleCRLF(body, pos);
            if (headerEnd == -1) break;
            
            String headerSection = new String(body, pos, headerEnd - pos, StandardCharsets.UTF_8);
            pos = headerEnd + 4;

            String name = extractFieldName(headerSection);
            boolean isFile = headerSection.contains("filename=");
            
            int nextBoundary = indexOf(body, delimiterBytes, pos);
            if (nextBoundary == -1) {
                nextBoundary = body.length;
            }

            int contentEnd = nextBoundary - 2;
            if (contentEnd < pos) contentEnd = pos;

            int contentLength = contentEnd - pos;
            
            if (name != null && contentLength > 0) {
                byte[] content = new byte[contentLength];
                System.arraycopy(body, pos, content, 0, contentLength);

                if (isFile) {
                    result.addFileField(name, content);
                } else {
                    result.addTextField(name, new String(content, StandardCharsets.UTF_8));
                }
            } else if (name != null) {
                if (isFile) {
                    result.addFileField(name, new byte[0]);
                } else {
                    result.addTextField(name, "");
                }
            }

            pos = nextBoundary + delimiterBytes.length;
            
            if (pos >= 2 && body[pos - delimiterBytes.length - 2] == '-' && body[pos - delimiterBytes.length - 1] == '-') {
                break;
            }
            
            if (pos < body.length - 1 && body[pos] == '\r' && body[pos + 1] == '\n') {
                pos += 2;
            } else if (pos < body.length && (body[pos] == '-' && pos + 1 < body.length && body[pos + 1] == '-')) {
                break;
            }
        }

        return result;
    }

    private static String extractFieldName(String headers) {
        int nameStart = headers.indexOf("name=\"");
        if (nameStart == -1) return null;
        nameStart += 6;
        int nameEnd = headers.indexOf("\"", nameStart);
        if (nameEnd == -1) return null;
        return headers.substring(nameStart, nameEnd);
    }

    private static int indexOf(byte[] data, byte[] pattern, int start) {
        outer:
        for (int i = start; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static int findDoubleCRLF(byte[] data, int start) {
        for (int i = start; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    public static String extractBoundary(String contentType) {
        if (contentType == null) return null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return part.substring("boundary=".length());
            }
        }
        return null;
    }
}
