package com.heber.polizas.adapter.out.persistence;

import com.heber.polizas.domain.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface SpringPolizaRepository extends JpaRepository<PolizaEntity, Long> {
  @EntityGraph(attributePaths = "riesgos")
  @Query(
      "select p from PolizaEntity p where (:tipo is null or p.tipo=:tipo) and (:estado is null or"
          + " p.estado=:estado)")
  List<PolizaEntity> filtrar(@Param("tipo") TipoPoliza tipo, @Param("estado") EstadoPoliza estado);

  @EntityGraph(attributePaths = "riesgos")
  @Query(
      "select p from PolizaEntity p where p.id=(select r.poliza.id from RiesgoEntity r where"
          + " r.id=:riesgoId)")
  Optional<PolizaEntity> buscarPorRiesgo(@Param("riesgoId") Long riesgoId);
}
