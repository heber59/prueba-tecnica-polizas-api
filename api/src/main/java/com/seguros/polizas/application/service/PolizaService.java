package com.seguros.polizas.application.service;

import com.seguros.polizas.application.exception.NoEncontradoException;
import com.seguros.polizas.application.port.in.GestionPolizas;
import com.seguros.polizas.application.port.out.*;
import com.seguros.polizas.domain.model.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class PolizaService implements GestionPolizas {
  private final PolizaRepository repository;
  private final CorePort core;
  private final Clock clock;

  public PolizaService(PolizaRepository repository, CorePort core, Clock clock) {
    this.repository = repository;
    this.core = core;
    this.clock = clock;
  }

  public List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado) {
    return repository.listar(tipo, estado);
  }

  public Poliza consultar(Long id) {
    return repository
        .buscar(id)
        .orElseThrow(() -> new NoEncontradoException("Póliza no encontrada"));
  }

  public synchronized Poliza renovar(Long id, BigDecimal ipc) {
    return sincronizar(consultar(id).renovar(ipc, LocalDate.now(clock)));
  }

  public synchronized Poliza cancelar(Long id) {
    var poliza = consultar(id);
    return poliza.estado() == EstadoPoliza.CANCELADA ? poliza : sincronizar(poliza.cancelar());
  }

  public synchronized Poliza agregarRiesgo(Long id, Riesgo riesgo) {
    return sincronizar(consultar(id).agregar(riesgo));
  }

  public synchronized Poliza cancelarRiesgo(Long riesgoId) {
    var poliza =
        repository
            .buscarPorRiesgo(riesgoId)
            .orElseThrow(() -> new NoEncontradoException("Riesgo no encontrado"));
    boolean cancelado =
        poliza.riesgos().stream()
            .anyMatch(r -> r.id().equals(riesgoId) && r.estado() == EstadoRiesgo.CANCELADO);
    return cancelado ? poliza : sincronizar(poliza.cancelarRiesgo(riesgoId));
  }

  private Poliza sincronizar(Poliza poliza) {
    core.actualizar(poliza.id());
    return repository.guardar(poliza);
  }
}
