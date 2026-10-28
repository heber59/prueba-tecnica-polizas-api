package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.exception.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record Poliza(
    Long id,
    String numero,
    TipoPoliza tipo,
    EstadoPoliza estado,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    int duracionMeses,
    List<Riesgo> riesgos) {
  public Poliza {
    riesgos = List.copyOf(riesgos);
    if (tipo == TipoPoliza.INDIVIDUAL && riesgos.size() != 1)
      throw new ReglaNegocioException("Una póliza individual debe tener exactamente un riesgo");
  }

  public BigDecimal canonMensual() {
    return riesgos.stream()
        .filter(r -> r.estado() == EstadoRiesgo.ACTIVO)
        .map(Riesgo::canonMensual)
        .reduce(new BigDecimal("0.00"), BigDecimal::add);
  }

  public BigDecimal prima() {
    return canonMensual().multiply(BigDecimal.valueOf(duracionMeses));
  }

  public Poliza renovar(BigDecimal ipc, LocalDate hoy) {
    exigirEditable();
    if (hoy.isBefore(fechaFin)) throw new ReglaNegocioException("La vigencia aún no ha terminado");
    if (ipc == null || ipc.signum() < 0 || ipc.compareTo(new BigDecimal("100")) > 0)
      throw new ReglaNegocioException("El IPC debe estar entre 0 y 100");
    if (riesgos.stream().noneMatch(r -> r.estado() == EstadoRiesgo.ACTIVO))
      throw new ReglaNegocioException("No hay riesgos activos para renovar");
    BigDecimal factor = BigDecimal.ONE.add(ipc.movePointLeft(2));
    return new Poliza(
        id,
        numero,
        tipo,
        EstadoPoliza.RENOVADA,
        fechaFin,
        fechaFin.plusMonths(duracionMeses),
        duracionMeses,
        riesgos.stream().map(r -> r.renovar(factor)).toList());
  }

  public Poliza cancelar() {
    return new Poliza(
        id,
        numero,
        tipo,
        EstadoPoliza.CANCELADA,
        fechaInicio,
        fechaFin,
        duracionMeses,
        riesgos.stream().map(Riesgo::cancelar).toList());
  }

  public Poliza agregar(Riesgo riesgo) {
    exigirEditable();
    if (tipo != TipoPoliza.COLECTIVA)
      throw new ReglaNegocioException("Solo se agregan riesgos a pólizas colectivas");
    var nuevos = new ArrayList<>(riesgos);
    nuevos.add(riesgo);
    return new Poliza(id, numero, tipo, estado, fechaInicio, fechaFin, duracionMeses, nuevos);
  }

  public Poliza cancelarRiesgo(Long riesgoId) {
    return new Poliza(
        id,
        numero,
        tipo,
        estado,
        fechaInicio,
        fechaFin,
        duracionMeses,
        riesgos.stream().map(r -> r.id().equals(riesgoId) ? r.cancelar() : r).toList());
  }

  private void exigirEditable() {
    if (estado == EstadoPoliza.CANCELADA)
      throw new ReglaNegocioException("La póliza está cancelada");
  }
}
