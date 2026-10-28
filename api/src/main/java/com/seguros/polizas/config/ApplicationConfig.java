package com.seguros.polizas.config;

import com.seguros.polizas.adapter.out.core.HttpCoreAdapter;
import com.seguros.polizas.application.port.in.GestionPolizas;
import com.seguros.polizas.application.port.out.*;
import com.seguros.polizas.application.service.PolizaService;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ApplicationConfig {
  @Bean
  Clock clock() {
    return Clock.system(ZoneId.of("America/Bogota"));
  }

  @Bean
  CorePort corePort(@Value("${core.base-url}") String url, @Value("${app.api-key}") String key) {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(2000);
    factory.setReadTimeout(3000);
    return new HttpCoreAdapter(
        RestClient.builder()
            .baseUrl(url)
            .defaultHeader("x-api-key", key)
            .requestFactory(factory)
            .build());
  }

  @Bean
  GestionPolizas gestionPolizas(PolizaRepository repository, CorePort core, Clock clock) {
    return new PolizaService(repository, core, clock);
  }
}
