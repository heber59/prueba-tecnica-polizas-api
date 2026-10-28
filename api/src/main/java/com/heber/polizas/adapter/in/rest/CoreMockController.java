package com.heber.polizas.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class CoreMockController {
  private static final Logger log = LoggerFactory.getLogger(CoreMockController.class);

  @PostMapping("/core-mock/evento")
  @SecurityRequirement(name = "apiKey")
  @Operation(summary = "Registrar intento de actualización del CORE")
  public Map<String, String> registrar(@Valid @RequestBody Evento request) {
    log.info(
        "Intento de envío al CORE: evento={}, polizaId={}", request.evento(), request.polizaId());
    return Map.of("estado", "RECIBIDO");
  }

  public record Evento(
      @NotBlank @Pattern(regexp = "ACTUALIZACION") String evento,
      @NotNull @Positive Long polizaId) {}
}
