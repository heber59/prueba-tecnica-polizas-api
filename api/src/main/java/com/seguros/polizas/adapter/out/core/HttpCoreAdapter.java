package com.seguros.polizas.adapter.out.core;

import com.seguros.polizas.application.exception.CoreException;
import com.seguros.polizas.application.port.out.CorePort;
import java.util.Map;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class HttpCoreAdapter implements CorePort {
  private final RestClient client;

  public HttpCoreAdapter(RestClient client) {
    this.client = client;
  }

  public void actualizar(Long polizaId) {
    try {
      client
          .post()
          .uri("/core-mock/evento")
          .body(Map.of("evento", "ACTUALIZACION", "polizaId", polizaId))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      throw new CoreException("No fue posible actualizar el CORE", ex);
    }
  }
}
