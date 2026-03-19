package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class H2ConsoleIntegrationTest {

    @LocalServerPort
    private int port;

    @DisplayName("/h2-console 요청은 H2 콘솔 경로로 리다이렉트된다")
    @Test
    void h2Console_redirectsToTrailingSlash() throws IOException, InterruptedException {
        var response = sendGet("/h2-console");

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location"))
                .hasValue("http://localhost:" + port + "/h2-console/");
    }

    @DisplayName("/h2-console/ 요청은 정적 리소스 404가 아니라 H2 콘솔 서블릿이 처리한다")
    @Test
    void h2Console_withTrailingSlash_isHandledByServlet() throws IOException, InterruptedException {
        var response = sendGet("/h2-console/");

        assertThat(response.statusCode()).isBetween(200, 399);
        assertThat(response.body()).doesNotContain("No static resource");
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        var client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
