package com.heber.polizas.application.port.in;

import com.heber.polizas.domain.model.*;
import java.math.BigDecimal;
import java.util.List;

public interface GestionPolizas {
  List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado);

  Poliza consultar(Long id);

  Poliza renovar(Long id, BigDecimal ipc);

  Poliza cancelar(Long id);

  Poliza agregarRiesgo(Long id, Riesgo riesgo);

  Poliza cancelarRiesgo(Long riesgoId);
}
