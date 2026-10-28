package com.heber.polizas.adapter.out.persistence;

import com.heber.polizas.application.port.out.PolizaRepository;
import com.heber.polizas.domain.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JpaPolizaAdapter implements PolizaRepository {
  private final SpringPolizaRepository repository;

  public JpaPolizaAdapter(SpringPolizaRepository repository) {
    this.repository = repository;
  }

  public List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado) {
    return repository.filtrar(tipo, estado).stream().map(this::mapear).toList();
  }

  public Optional<Poliza> buscar(Long id) {
    return repository.findById(id).map(this::mapear);
  }

  public Optional<Poliza> buscarPorRiesgo(Long riesgoId) {
    return repository.buscarPorRiesgo(riesgoId).map(this::mapear);
  }

  public Poliza guardar(Poliza poliza) {
    var entity =
        poliza.id() == null ? new PolizaEntity() : repository.findById(poliza.id()).orElseThrow();
    entity.numero = poliza.numero();
    entity.tipo = poliza.tipo();
    entity.estado = poliza.estado();
    entity.fechaInicio = poliza.fechaInicio();
    entity.fechaFin = poliza.fechaFin();
    entity.duracionMeses = poliza.duracionMeses();
    for (var riesgo : poliza.riesgos()) {
      var child =
          entity.riesgos.stream()
              .filter(r -> riesgo.id() != null && riesgo.id().equals(r.id))
              .findFirst()
              .orElseGet(
                  () -> {
                    var r = new RiesgoEntity();
                    r.poliza = entity;
                    entity.riesgos.add(r);
                    return r;
                  });
      child.direccion = riesgo.direccion();
      child.arrendatario = riesgo.arrendatario();
      child.beneficiario = riesgo.beneficiario();
      child.estado = riesgo.estado();
      child.canonMensual = riesgo.canonMensual();
    }
    return mapear(repository.saveAndFlush(entity));
  }

  private Poliza mapear(PolizaEntity p) {
    return new Poliza(
        p.id,
        p.numero,
        p.tipo,
        p.estado,
        p.fechaInicio,
        p.fechaFin,
        p.duracionMeses,
        p.riesgos.stream()
            .map(
                r ->
                    new Riesgo(
                        r.id,
                        r.direccion,
                        r.arrendatario,
                        r.beneficiario,
                        r.estado,
                        r.canonMensual))
            .toList());
  }
}
