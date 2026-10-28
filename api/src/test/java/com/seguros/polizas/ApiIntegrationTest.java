package com.seguros.polizas;

import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {
  private static final AtomicInteger coreStatus = new AtomicInteger(200);
  private static final AtomicInteger calls = new AtomicInteger();
  private static final AtomicReference<String> payload = new AtomicReference<>();
  private static final AtomicReference<String> apiKey = new AtomicReference<>();
  private static final HttpServer core;

  static {
    try {
      core = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
      core.createContext(
          "/core-mock/evento",
          exchange -> {
            calls.incrementAndGet();
            payload.set(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            apiKey.set(exchange.getRequestHeaders().getFirst("x-api-key"));
            exchange.sendResponseHeaders(coreStatus.get(), -1);
            exchange.close();
          });
      core.start();
    } catch (Exception ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("core.base-url", () -> "http://localhost:" + core.getAddress().getPort());
  }

  @AfterAll
  static void shutdown() {
    core.stop(0);
  }

  @Autowired TestRestTemplate http;
  @Autowired ObjectMapper mapper;

  private ResponseEntity<JsonNode> request(
      HttpMethod method, String url, String body, boolean auth) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (auth) headers.set("x-api-key", "123456");
    return http.exchange(url, method, new HttpEntity<>(body, headers), JsonNode.class);
  }

  @Test
  void saludYDocumentacionPublicas() {
    assertThat(http.getForEntity("/health", String.class).getBody()).contains("UP");
    var docs = http.getForEntity("/v3/api-docs", JsonNode.class);
    assertThat(docs.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(docs.getBody().path("paths").has("/polizas/{id}/renovar")).isTrue();
    assertThat(docs.getBody().path("paths").path("/polizas").path("get").path("summary").asText())
        .isEqualTo("Listar pólizas por tipo y estado");
    assertThat(docs.getBody().path("paths").path("/polizas").path("get").path("tags").get(0).asText())
        .isEqualTo("Pólizas y riesgos");
    assertThat(docs.getBody().path("components").path("securitySchemes").has("apiKey")).isTrue();
    assertThat(http.getForEntity("/swagger-ui/index.html", String.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void requiereApiKey() {
    assertThat(request(HttpMethod.GET, "/polizas", null, false).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
    var headers = new HttpHeaders();
    headers.set("x-api-key", "incorrecta");
    assertThat(
            http.exchange("/polizas", HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void recorridoCompleto() throws Exception {
    var list = request(HttpMethod.GET, "/polizas?tipo=INDIVIDUAL&estado=ACTIVA", null, true);
    assertThat(list.getBody().size()).isEqualTo(1);
    long individual = list.getBody().get(0).path("id").asLong();
    long colectiva =
        request(HttpMethod.GET, "/polizas?tipo=COLECTIVA", null, true)
            .getBody()
            .get(0)
            .path("id")
            .asLong();
    String nuevo =
        "{\"direccion\":\"Calle"
            + " nueva\",\"arrendatario\":\"Ana\",\"beneficiario\":\"Luis\",\"canonMensual\":1000}";
    assertThat(
            request(HttpMethod.POST, "/polizas/" + individual + "/riesgos", nuevo, true)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
    assertThat(
            request(HttpMethod.POST, "/polizas/" + colectiva + "/riesgos", "{}", true)
                .getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
    var added = request(HttpMethod.POST, "/polizas/" + colectiva + "/riesgos", nuevo, true);
    assertThat(added.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var riesgos = added.getBody().path("riesgos");
    long riesgoId = riesgos.get(riesgos.size() - 1).path("id").asLong();
    assertThat(
            request(HttpMethod.POST, "/riesgos/" + riesgoId + "/cancelar", null, true)
                .getStatusCode())
        .isEqualTo(HttpStatus.OK);
    var renovada =
        request(HttpMethod.POST, "/polizas/" + individual + "/renovar", "{\"ipc\":5.2}", true);
    assertThat(renovada.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(renovada.getBody().path("canonMensual").decimalValue())
        .isEqualByComparingTo("1052000.00");
    assertThat(renovada.getBody().path("prima").decimalValue()).isEqualByComparingTo("12624000.00");
    assertThat(apiKey.get()).isEqualTo("123456");
    assertThat(mapper.readTree(payload.get()).path("polizaId").asLong()).isEqualTo(individual);
    assertThat(
            request(HttpMethod.POST, "/polizas/" + individual + "/renovar", "{\"ipc\":5.2}", true)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
    coreStatus.set(503);
    try {
      assertThat(
              request(HttpMethod.POST, "/polizas/" + colectiva + "/cancelar", null, true)
                  .getStatusCode())
          .isEqualTo(HttpStatus.BAD_GATEWAY);
      assertThat(
              request(HttpMethod.GET, "/polizas?tipo=COLECTIVA&estado=ACTIVA", null, true)
                  .getBody()
                  .size())
          .isEqualTo(1);
    } finally {
      coreStatus.set(200);
    }
    assertThat(
            request(HttpMethod.POST, "/polizas/" + colectiva + "/cancelar", null, true)
                .getStatusCode())
        .isEqualTo(HttpStatus.OK);
    var cancelados =
        request(HttpMethod.GET, "/polizas/" + colectiva + "/riesgos", null, true).getBody();
    cancelados.forEach(r -> assertThat(r.path("estado").asText()).isEqualTo("CANCELADO"));
    int previous = calls.get();
    request(HttpMethod.POST, "/polizas/" + colectiva + "/cancelar", null, true);
    assertThat(calls.get()).isEqualTo(previous);
    assertThat(
            request(HttpMethod.POST, "/polizas/" + colectiva + "/renovar", "{\"ipc\":5}", true)
                .getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void erroresYMock() {
    assertThat(request(HttpMethod.GET, "/polizas?tipo=OTRO", null, true).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(request(HttpMethod.GET, "/polizas/999/riesgos", null, true).getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(
            request(
                    HttpMethod.POST,
                    "/core-mock/evento",
                    "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}",
                    true)
                .getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(request(HttpMethod.POST, "/core-mock/evento", "{}", true).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
