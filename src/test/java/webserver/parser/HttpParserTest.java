package webserver.parser;

import model.HttpMethod;
import model.ParsedHttpRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

class HttpParserTest {

    @Test
    void parseGetRequestWithQueryParameters() throws Exception {
        String raw =
                "GET /create?userId=abc&name=kim&password=1q2w3e4r&email=jjds@fsd.com HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "\r\n";

        HttpParser parser = new HttpParser();

        ParsedHttpRequest req = parser.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));

        assertThat(req.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(req.getPath()).isEqualTo("/create");
        assertThat(req.getQueryParameters())
                .containsEntry("userId", "abc")
                .containsEntry("name", "kim")
                .containsEntry("password", "1q2w3e4r")
                .containsEntry("email", "jjds@fsd.com");
    }

    @Test
    void parsePostRequestBodyUsingContentLength() throws Exception {
        String body = "email=test@test.com";
        String raw =
                "POST /login HTTP/1.1\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "\r\n" +
                        body;

        HttpParser parser = new HttpParser();

        ParsedHttpRequest req = parser.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));

        assertThat(req.getMethod()).isEqualTo(HttpMethod.POST);
        assertThat(req.getPath()).isEqualTo("/login");
        assertThat(req.getBody()).isEqualTo(body);
    }

    @Test
    void returnEmptyBodyWhenContentLengthIsMissing() throws Exception {
        String raw =
                "GET / HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "\r\n";

        HttpParser parser = new HttpParser();

        ParsedHttpRequest req = parser.parse(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));

        assertThat(req.getBody()).isEmpty();
    }
}