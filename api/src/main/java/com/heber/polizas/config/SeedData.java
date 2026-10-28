package com.heber.polizas.config;

import com.heber.polizas.application.port.out.PolizaRepository;
import com.heber.polizas.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class SeedData {
  @Bean
  CommandLineRunner seed(PolizaRepository repository, Clock clock) {
    return args -> {
      if (!repository.listar(null, null).isEmpty()) return;
      LocalDate fin = LocalDate.now(clock);
      repository.guardar(
          new Poliza(
              null,
              "IND-001",
              TipoPoliza.INDIVIDUAL,
              EstadoPoliza.ACTIVA,
              fin.minusMonths(12),
              fin,
              12,
              List.of(riesgo("Calle 1", "1000000.00"))));
      repository.guardar(
          new Poliza(
              null,
              "COL-001",
              TipoPoliza.COLECTIVA,
              EstadoPoliza.ACTIVA,
              fin.minusMonths(12),
              fin,
              12,
              List.of(riesgo("Calle 2", "1500000.00"), riesgo("Calle 3", "2000000.00"))));
    };
  }

  private Riesgo riesgo(String direccion, String canon) {
    return new Riesgo(
        null,
        direccion,
        "Arrendatario de ejemplo",
        "Arrendador de ejemplo",
        EstadoRiesgo.ACTIVO,
        new BigDecimal(canon));
  }
}
