package br.com.emersonfarias.springsercurity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringSercurityApplicationTests {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @LocalServerPort
    private int port;

    @Test
    void shouldServeDefaultLoginPage() throws Exception {
        HttpResponse<String> response = get("/login", "text/html");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
    }

    @Test
    void shouldRequireAuthenticationForJsonRequests() throws Exception {
        HttpResponse<String> response = get("/", "application/json");

        assertEquals(401, response.statusCode());
        assertTrue(response.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Basic "));
    }

    @Test
    void shouldRedirectBrowserRequestsToLogin() throws Exception {
        HttpResponse<String> response = get("/", "text/html");

        assertEquals(302, response.statusCode());
        assertEquals("/login", URI.create(response.headers().firstValue("Location").orElseThrow()).getPath());
    }

    private HttpResponse<String> get(String path, String accept) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", accept)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
