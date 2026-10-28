package com.heber.polizas.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Riesgo(
    Long id,
    String direccion,
    String arrendatario,
    String beneficiario,
    EstadoRiesgo estado,
    BigDecimal canonMensual) {
  public Riesgo cancelar() {
    return new Riesgo(
        id, direccion, arrendatario, beneficiario, EstadoRiesgo.CANCELADO, canonMensual);
  }

  public Riesgo renovar(BigDecimal factor) {
    return estado == EstadoRiesgo.CANCELADO
        ? this
        : new Riesgo(
            id,
            direccion,
            arrendatario,
            beneficiario,
            estado,
            canonMensual.multiply(factor).setScale(2, RoundingMode.HALF_UP));
  }
}
