package com.seguros.polizas.adapter.in.rest;

import com.seguros.polizas.application.port.in.GestionPolizas;
import com.seguros.polizas.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Pólizas y riesgos", description = "Consulta y gestión de pólizas de arrendamiento")
@SecurityRequirement(name = "apiKey")
@ApiResponses({
  @ApiResponse(responseCode = "400", description = "Entrada invÃ¡lida"),
  @ApiResponse(responseCode = "401", description = "API key ausente o incorrecta"),
  @ApiResponse(responseCode = "404", description = "Recurso no encontrado"),
  @ApiResponse(responseCode = "409", description = "Regla de negocio incumplida"),
  @ApiResponse(responseCode = "502", description = "Error de integraciÃ³n CORE")
})
public class PolizaController {
  private final GestionPolizas gestion;

  public PolizaController(GestionPolizas gestion) {
    this.gestion = gestion;
  }

  @GetMapping("/polizas")
  @Operation(summary = "Listar pólizas por tipo y estado")
  public List<PolizaResponse> listar(
      @RequestParam(required = false) TipoPoliza tipo,
      @RequestParam(required = false) EstadoPoliza estado) {
    return gestion.listar(tipo, estado).stream().map(PolizaResponse::de).toList();
  }

  @GetMapping("/polizas/{id}/riesgos")
  @Operation(summary = "Consultar riesgos de una póliza")
  public List<Riesgo> riesgos(@PathVariable Long id) {
    return gestion.consultar(id).riesgos();
  }

  @PostMapping("/polizas/{id}/renovar")
  @Operation(summary = "Renovar una póliza vencida aplicando IPC")
  public PolizaResponse renovar(
      @PathVariable Long id, @Valid @RequestBody RenovacionRequest request) {
    return PolizaResponse.de(gestion.renovar(id, request.ipc()));
  }

  @PostMapping("/polizas/{id}/cancelar")
  @Operation(summary = "Cancelar póliza y riesgos")
  public PolizaResponse cancelar(@PathVariable Long id) {
    return PolizaResponse.de(gestion.cancelar(id));
  }

  @PostMapping("/polizas/{id}/riesgos")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Agregar un riesgo a una póliza colectiva")
  public PolizaResponse agregar(@PathVariable Long id, @Valid @RequestBody RiesgoRequest request) {
    return PolizaResponse.de(
        gestion.agregarRiesgo(
            id,
            new Riesgo(
                null,
                request.direccion(),
                request.arrendatario(),
                request.beneficiario(),
                EstadoRiesgo.ACTIVO,
                request.canonMensual())));
  }

  @PostMapping("/riesgos/{id}/cancelar")
  @Operation(summary = "Cancelar un riesgo")
  public PolizaResponse cancelarRiesgo(@PathVariable Long id) {
    return PolizaResponse.de(gestion.cancelarRiesgo(id));
  }

  public record RenovacionRequest(
      @NotNull @DecimalMin("0") @DecimalMax("100") @Schema(example = "5.2") BigDecimal ipc) {}

  public record RiesgoRequest(
      @NotBlank @Size(max = 250) @Schema(example = "Calle 10 # 20-30") String direccion,
      @NotBlank @Size(max = 150) @Schema(example = "Ana PÃ©rez") String arrendatario,
      @NotBlank @Size(max = 150) @Schema(example = "Luis GÃ³mez") String beneficiario,
      @NotNull
          @DecimalMin("0.01")
          @Digits(integer = 16, fraction = 2)
          @Schema(example = "1500000.00")
          BigDecimal canonMensual) {}

  public record PolizaResponse(
      Long id,
      String numero,
      TipoPoliza tipo,
      EstadoPoliza estado,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      int duracionMeses,
      BigDecimal canonMensual,
      BigDecimal prima,
      List<Riesgo> riesgos) {
    static PolizaResponse de(Poliza p) {
      return new PolizaResponse(
          p.id(),
          p.numero(),
          p.tipo(),
          p.estado(),
          p.fechaInicio(),
          p.fechaFin(),
          p.duracionMeses(),
          p.canonMensual(),
          p.prima(),
          p.riesgos());
    }
  }
}
