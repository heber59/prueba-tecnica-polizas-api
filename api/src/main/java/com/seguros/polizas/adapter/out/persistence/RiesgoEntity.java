package com.seguros.polizas.adapter.out.persistence;

import com.seguros.polizas.domain.model.EstadoRiesgo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "riesgos")
public class RiesgoEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String direccion;
  public String arrendatario;
  public String beneficiario;

  @Enumerated(EnumType.STRING)
  public EstadoRiesgo estado;

  @Column(precision = 18, scale = 2, nullable = false)
  public BigDecimal canonMensual;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "poliza_id")
  public PolizaEntity poliza;
}
