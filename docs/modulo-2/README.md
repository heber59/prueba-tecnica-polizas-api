# Módulo 2: API de gestión de pólizas

## Arquitectura hexagonal

```text
com/seguros/polizas/
|-- domain/
|   |-- model/                Poliza, Riesgo y estados
|   `-- exception/            Errores de negocio
|-- application/
|   |-- port/in/              GestionPolizas
|   |-- port/out/             PolizaRepository y CorePort
|   |-- service/              PolizaService
|   `-- exception/            Recurso inexistente y fallo CORE
|-- adapter/
|   |-- in/rest/              Controllers, DTO y manejo de errores
|   `-- out/
|       |-- persistence/      Entidades JPA, repository y adaptador
|       `-- core/             HttpCoreAdapter
`-- config/                   Beans, API key, Swagger y datos iniciales
```

| Parte                  | Responsabilidad en esta API                                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| Dominio                | Poliza y Riesgo contienen las reglas y los cálculos. No importan Spring, JPA ni clases HTTP.                              |
| Puerto de entrada      | GestionPolizas define las operaciones que puede solicitar el controller.                                                  |
| Servicio de aplicación | PolizaService implementa GestionPolizas: consulta datos, ejecuta reglas, comunica el cambio al CORE y solicita guardarlo. |
| Puertos de salida      | PolizaRepository define el acceso a las pólizas; CorePort define el envío de actualizaciones al CORE.                     |
| Adaptador REST         | PolizaController recibe solicitudes y devuelve respuestas. CoreMockController recibe los eventos del mock.                |
| Adaptador JPA          | JpaPolizaAdapter implementa PolizaRepository, convierte entidades y usa SpringPolizaRepository para acceder a H2.         |
| Adaptador CORE         | HttpCoreAdapter implementa CorePort y envía la solicitud HTTP al mock.                                                    |
| Configuración          | ApplicationConfig crea el servicio y le entrega los adaptadores y el reloj.                                               |

### Ejemplo: renovar una póliza

1. PolizaController recibe el identificador y el IPC y llama a GestionPolizas.
2. PolizaService obtiene la póliza mediante PolizaRepository.
3. Poliza valida su estado y vigencia, aplica el IPC y devuelve la nueva versión.
4. El servicio llama a CorePort; HttpCoreAdapter envía el evento al CORE mock.
5. Si el CORE responde correctamente, el servicio guarda mediante PolizaRepository; JpaPolizaAdapter persiste el cambio en H2.
6. El controller devuelve el resultado mediante su DTO de respuesta.

## Endpoints

| Método y ruta               | Comportamiento                                          |
| --------------------------- | ------------------------------------------------------- |
| GET /polizas?tipo=&estado=  | Listar con filtros opcionales y combinables.            |
| GET /polizas/{id}/riesgos   | Consultar riesgos y sus estados.                        |
| POST /polizas/{id}/renovar  | Aplicar IPC, ajustar vigencia y cambiar a RENOVADA.     |
| POST /polizas/{id}/cancelar | Cancelar póliza y todos sus riesgos activos.            |
| POST /polizas/{id}/riesgos  | Agregar riesgo únicamente a colectiva en estado válido. |
| POST /riesgos/{id}/cancelar | Cancelar un riesgo sin eliminar su registro.            |
| POST /core-mock/evento      | Registrar en logs el intento de actualización al CORE.  |
| GET /health                 | Informar salud mediante Actuator.                       |

## Reglas y decisiones

- Una individual tiene exactamente un riesgo; agregar riesgos por API solo se permite en colectivas.
- Una póliza cancelada no puede renovarse ni recibir riesgos.
- Cancelar póliza cancela todos sus riesgos activos.
- Renovar recibe un JSON con `ipc`, por ejemplo `{"ipc": 5.2}`: representa 5,2 %.
- Nuevo canon = canon anterior × (1 + IPC / 100).
- Prima = nuevo canon × meses de vigencia. En colectivas se calcula por riesgo activo y se suman los importes.
- Usar BigDecimal, dos decimales y redondeo HALF_UP.
- Renovación mantiene la duración inicial y comienza al finalizar la vigencia anterior; los riesgos cancelados no se reactivan.
- Estados de póliza: ACTIVA, RENOVADA y CANCELADA. Estados de riesgo: ACTIVO y CANCELADO.
- Cancelar algo ya cancelado devuelve su estado sin repetir la actualización al CORE.
- Cancelar el último riesgo no cancela automáticamente la póliza: supuesto de la práctica.
- No se exige Idempotency-Key en esta versión. Solo se permite renovar una vigencia terminada; las nuevas fechas impiden repetir inmediatamente la renovación. No equivale a idempotencia completa.

## Errores

| Código | Uso                                       |
| ------ | ----------------------------------------- |
| 400    | Datos, filtros o IPC inválidos.           |
| 401    | API key ausente o incorrecta.             |
| 404    | Póliza o riesgo inexistente.              |
| 409    | Operación incompatible con tipo o estado. |
| 502    | Fallo al consumir CORE.                   |

## Pruebas y ejecución

- Pruebas del dominio para IPC, restricciones y cancelación en cascada.
- Pruebas de API para filtros, validaciones, estados HTTP y API key.
- Pruebas del adaptador para comprobar la solicitud HTTP al CORE y el manejo de fallos.
- Comprobación de /health y documentación OpenAPI.
- Datos iniciales y ejemplos reproducibles en Swagger.
