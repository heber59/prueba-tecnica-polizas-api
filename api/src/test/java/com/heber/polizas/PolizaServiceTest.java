package com.heber.polizas;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heber.polizas.application.exception.CoreException;
import com.heber.polizas.application.port.out.*;
import com.heber.polizas.application.service.PolizaService;
import com.heber.polizas.domain.exception.ReglaNegocioException;
import com.heber.polizas.domain.model.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class PolizaServiceTest {
  private final PolizaRepository repository = mock(PolizaRepository.class);
  private final CorePort core = mock(CorePort.class);
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC);
  private final PolizaService service = new PolizaService(repository, core, clock);

  private Poliza poliza(TipoPoliza tipo) {
    return new Poliza(
        1L,
        "P-1",
        tipo,
        EstadoPoliza.ACTIVA,
        LocalDate.of(2025, 10, 28),
        LocalDate.of(2026, 10, 28),
        12,
        List.of(
            new Riesgo(
                1L, "Calle 1", "Ana", "Luis", EstadoRiesgo.ACTIVO, new BigDecimal("1000.00"))));
  }

  @Test
  void renuevaConIpcYConservaDuracion() {
    var p = poliza(TipoPoliza.INDIVIDUAL).renovar(new BigDecimal("5.2"), LocalDate.now(clock));
    assertThat(p.canonMensual()).isEqualByComparingTo("1052.00");
    assertThat(p.prima()).isEqualByComparingTo("12624.00");
    assertThat(p.fechaInicio()).isEqualTo(LocalDate.of(2026, 10, 28));
    assertThat(p.fechaFin()).isEqualTo(LocalDate.of(2027, 10, 28));
    assertThat(p.estado()).isEqualTo(EstadoPoliza.RENOVADA);
    assertThatThrownBy(() -> p.renovar(BigDecimal.ONE, LocalDate.now(clock)))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void noRenuevaCancelada() {
    assertThatThrownBy(
            () ->
                poliza(TipoPoliza.INDIVIDUAL)
                    .cancelar()
                    .renovar(BigDecimal.ONE, LocalDate.now(clock)))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void cancelaTodosLosRiesgos() {
    assertThat(poliza(TipoPoliza.COLECTIVA).cancelar().riesgos())
        .allMatch(r -> r.estado() == EstadoRiesgo.CANCELADO);
  }

  @Test
  void individualNoAdmiteRiesgosAdicionales() {
    var p = poliza(TipoPoliza.INDIVIDUAL);
    assertThatThrownBy(() -> p.agregar(p.riesgos().getFirst()))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void falloCoreNoGuardaCambios() {
    when(repository.buscar(1L)).thenReturn(Optional.of(poliza(TipoPoliza.INDIVIDUAL)));
    doThrow(new CoreException("CORE no disponible", new RuntimeException()))
        .when(core)
        .actualizar(1L);
    assertThatThrownBy(() -> service.cancelar(1L)).isInstanceOf(CoreException.class);
    verify(repository, never()).guardar(any());
  }

  @Test
  void cancelacionRepetidaNoInvocaCore() {
    var p = poliza(TipoPoliza.INDIVIDUAL).cancelar();
    when(repository.buscar(1L)).thenReturn(Optional.of(p));
    assertThat(service.cancelar(1L)).isEqualTo(p);
    verifyNoInteractions(core);
  }

  @Test
  void noRenuevaSinRiesgosActivos() {
    var p = poliza(TipoPoliza.COLECTIVA).cancelarRiesgo(1L);
    assertThatThrownBy(() -> p.renovar(BigDecimal.ONE, LocalDate.now(clock)))
        .isInstanceOf(ReglaNegocioException.class);
  }

  @Test
  void colectivaRenuevaSoloRiesgosActivosConRedondeo() {
    var activo = new Riesgo(1L, "A", "Ana", "Luis", EstadoRiesgo.ACTIVO, new BigDecimal("100.05"));
    var cancelado =
        new Riesgo(2L, "B", "Ana", "Luis", EstadoRiesgo.CANCELADO, new BigDecimal("500.00"));
    var p =
        new Poliza(
            1L,
            "C-1",
            TipoPoliza.COLECTIVA,
            EstadoPoliza.ACTIVA,
            LocalDate.of(2025, 10, 28),
            LocalDate.of(2026, 10, 28),
            12,
            List.of(activo, cancelado));
    var renovada = p.renovar(new BigDecimal("10"), LocalDate.now(clock));
    assertThat(renovada.canonMensual()).isEqualByComparingTo("110.06");
    assertThat(renovada.prima()).isEqualByComparingTo("1320.72");
    assertThat(renovada.riesgos().get(1).estado()).isEqualTo(EstadoRiesgo.CANCELADO);
    assertThat(renovada.riesgos().get(1).canonMensual()).isEqualByComparingTo("500.00");
  }

  @Test
  void rechazaIpcFueraDeRango() {
    var p = poliza(TipoPoliza.INDIVIDUAL);
    assertThatThrownBy(() -> p.renovar(new BigDecimal("-1"), LocalDate.now(clock)))
        .isInstanceOf(ReglaNegocioException.class);
    assertThatThrownBy(() -> p.renovar(new BigDecimal("101"), LocalDate.now(clock)))
        .isInstanceOf(ReglaNegocioException.class);
  }
}
