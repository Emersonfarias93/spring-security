package br.com.emersonfarias.springsercurity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.security.username=demo-test",
        "app.security.password=test-only-password"
})
class SpringSercurityApplicationTests {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAllowPublicQueryWithoutCredentials() throws Exception {
        HttpResponse<String> response = get("/api/public/hello", null, "application/json");

        assertEquals(200, response.statusCode());
        assertJson(response);
        assertEquals(objectMapper.readTree("{\"message\":\"Olá! Este endpoint é público.\"}"),
                objectMapper.readTree(response.body()));
    }

    @Test
    void shouldIdentifyUserWithoutExposingCredentials() throws Exception {
        HttpResponse<String> response = get("/api/me", basic("demo-test", "test-only-password"), "application/json");

        assertEquals(200, response.statusCode());
        assertJson(response);
        assertEquals(objectMapper.readTree("{\"username\":\"demo-test\",\"roles\":[\"USER\"]}"),
                objectMapper.readTree(response.body()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "text/html"})
    void shouldReturnJsonInsteadOfRedirectingUnauthenticatedClients(String accept) throws Exception {
        assertUnauthorized(get("/api/me", null, accept));
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {
        assertUnauthorized(get("/api/me", basic("demo-test", "wrong-password"), "application/json"));
    }

    @Test
    void shouldRejectUnknownUser() throws Exception {
        assertUnauthorized(get("/api/me", basic("unknown-user", "test-only-password"), "application/json"));
    }

    @Test
    void shouldRejectMalformedBasicCredentials() throws Exception {
        assertUnauthorized(get("/api/me", "Basic !!!", "application/json"));
    }

    @Test
    void shouldAuthenticateEveryRequestWithoutReusingSession() throws Exception {
        HttpResponse<String> authenticated = get("/api/me", basic("demo-test", "test-only-password"), "application/json");

        assertEquals(200, authenticated.statusCode());
        assertTrue(authenticated.headers().allValues("Set-Cookie").isEmpty());
        assertUnauthorized(get("/api/me", null, "application/json"));
    }

    @Test
    void shouldKeepCsrfProtectionForUnsafeMethods() throws Exception {
        HttpRequest request = request("/api/me", basic("demo-test", "test-only-password"), "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(403, response.statusCode());
        assertJson(response);
        assertEquals(403, objectMapper.readTree(response.body()).get("status").asInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/unknown", "/api/public/unknown", "/api/admin/hello"})
    void shouldDenyRoutesOutsideTheExplicitAllowlist(String path) throws Exception {
        HttpResponse<String> response = get(path, basic("demo-test", "test-only-password"), "application/json");

        assertEquals(403, response.statusCode());
        assertJson(response);
        assertEquals(403, objectMapper.readTree(response.body()).get("status").asInt());
    }

    @Test
    void shouldNotExposeDefaultLoginForm() throws Exception {
        assertUnauthorized(get("/login", null, "text/html"));
    }

    private void assertUnauthorized(HttpResponse<String> response) {
        assertEquals(401, response.statusCode());
        assertJson(response);
        assertEquals("Basic realm=\"spring-security\"", response.headers().firstValue("WWW-Authenticate").orElseThrow());
        assertTrue(response.headers().firstValue("Location").isEmpty());
        assertEquals(objectMapper.readTree("""
                {"status":401,"error":"Unauthorized","message":"Autenticação necessária ou credenciais inválidas."}
                """), objectMapper.readTree(response.body()));
    }

    private void assertJson(HttpResponse<String> response) {
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
    }

    private HttpResponse<String> get(String path, String authorization, String accept) throws Exception {
        return httpClient.send(request(path, authorization, accept).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path, String authorization, String accept) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", accept);

        if (authorization != null) {
            builder.header("Authorization", authorization);
        }

        return builder;
    }

    private String basic(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

}
