package webserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class TemplateEngine {

    private static final String TEMPLATE_BASE =
            "src/main/resources/static/";

    private TemplateEngine() {
    }

    public static String render(String templateName,
                                Map<String, String> model) throws IOException {

        String html = Files.readString(
                Path.of(TEMPLATE_BASE + templateName),
                StandardCharsets.UTF_8
        );

        if (model == null || model.isEmpty()) {
            return html;
        }

        for (Map.Entry<String, String> entry : model.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() == null ? "" : entry.getValue();
            html = html.replace(key, value);
        }

        return html;
    }
}