package com.seguros.polizas.adapter.out.persistence;

import com.seguros.polizas.domain.model.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polizas")
public class PolizaEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String numero;

  @Enumerated(EnumType.STRING)
  public TipoPoliza tipo;

  @Enumerated(EnumType.STRING)
  public EstadoPoliza estado;

  public LocalDate fechaInicio;
  public LocalDate fechaFin;
  public int duracionMeses;

  @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<RiesgoEntity> riesgos = new ArrayList<>();
}
