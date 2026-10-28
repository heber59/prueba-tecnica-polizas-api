# Arquitectura de la plataforma de pólizas

| Componente            | Responsabilidad                                                            |
| --------------------- | -------------------------------------------------------------------------- |
| Frontend              | Presentar operaciones y consumir la API.                                   |
| API Gateway           | Enrutamiento, control de acceso y límites de tráfico.                      |
| Servicio de Pólizas   | Coordinar pólizas, vigencias y reglas de renovación/cancelación.           |
| Servicio de Riesgos   | Consultar, agregar y cancelar riesgos con validaciones de negocio.         |
| Proceso de Renovación | Detectar vigencias elegibles e invocar Pólizas con el IPC correspondiente. |
| Adaptador CORE        | Traducir operaciones al contrato de edición de WebLogic.                   |
| Base relacional       | Persistir dominio, intentos de integración y eventos Outbox.               |
| Publicador de eventos | Leer Outbox y publicar eventos pendientes.                                 |
| Broker                | Desacoplar publicación y consumo.                                          |
| Notificaciones        | Consumir eventos y solicitar correo/SMS al proveedor.                      |
| WebLogic              | Exponer el servicio agnóstico que actualiza el CORE.                       |
| CORE                  | Mantener la información transaccional del seguro.                          |

## Tres patrones seleccionados

1. **Hexagonal:** dominio dependiente de interfaces, con adaptadores para base, CORE y servicios externos. Permite pruebas aisladas y reemplazar CORE real por mock.
2. **Orientado a eventos:** desacopla las notificaciones del negocio. Outbox transaccional evita perder eventos entre persistencia y publicación; consumidores idempotentes toleran entregas duplicadas.
3. **API Gateway:** concentra acceso, enrutamiento y controles transversales; facilita límites de consumo y versionamiento.

## Atributos de calidad

| Aspecto             | Estrategia                                                                                                                                                        |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Escalabilidad       | Instancias sin estado, balanceo, paginación e índices según consultas; consumidores escalables y reclamación de tareas para evitar renovaciones duplicadas.       |
| Observabilidad      | Logs estructurados, correlación API/CORE/eventos, trazas y métricas de latencia, errores, Outbox y renovaciones pendientes. Evitar datos personales innecesarios. |
| Tolerancia a fallos | Timeouts, circuit breaker, reintentos transitorios limitados con espera exponencial, idempotencia, cola de mensajes fallidos y conciliación.                      |
| Disponibilidad 24/7 | Réplicas de aplicación, salud, despliegues graduales, base replicada y recuperación probada. Una caída del CORE puede dejar escrituras pendientes.                |
| Versionamiento      | /api/v1 en producción; nueva versión ante incompatibilidades, transición y esquema de eventos versionado. La práctica conserva las rutas exigidas.                |
| Seguridad           | TLS, autenticación y autorización por roles, auditoría y secretos externos. La API key requerida es el mínimo de demostración.                                    |
