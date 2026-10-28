package com.seguros.polizas.application.port.out;

import com.seguros.polizas.domain.model.*;
import java.util.List;
import java.util.Optional;

public interface PolizaRepository {
  List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado);

  Optional<Poliza> buscar(Long id);

  Optional<Poliza> buscarPorRiesgo(Long riesgoId);

  Poliza guardar(Poliza poliza);
}
