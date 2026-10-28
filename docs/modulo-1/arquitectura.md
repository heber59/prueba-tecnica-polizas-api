# Arquitectura de la plataforma de pólizas

## Objetivo

Crear, consultar, modificar, renovar y cancelar pólizas; administrar riesgos; notificar creación y renovación; consumir el servicio agnóstico de edición de WebLogic para actualizar el CORE.

## Componentes y conexiones

| Componente | Responsabilidad |
| --- | --- |
| Frontend | Presentar operaciones y consumir la API. |
| API Gateway | Enrutamiento, control de acceso y límites de tráfico. |
| Servicio de Pólizas | Coordinar pólizas, vigencias y reglas de renovación/cancelación. |
| Servicio de Riesgos | Consultar, agregar y cancelar riesgos con validaciones de negocio. |
| Proceso de Renovación | Detectar vigencias elegibles e invocar Pólizas con el IPC correspondiente. |
| Adaptador CORE | Traducir operaciones al contrato de edición de WebLogic. |
| Base relacional | Persistir dominio, intentos de integración y eventos Outbox. |
| Publicador de eventos | Leer Outbox y publicar eventos pendientes. |
| Broker | Desacoplar publicación y consumo. |
| Notificaciones | Consumir eventos y solicitar correo/SMS al proveedor. |
| WebLogic | Exponer el servicio agnóstico que actualiza el CORE. |
| CORE | Mantener la información transaccional del seguro. |

Conexiones del diagrama:

- Usuarios → Frontend → Gateway → Servicios de Pólizas y Riesgos.
- Proceso de Renovación → Servicio de Pólizas.
- Servicios de Pólizas y Riesgos → Base de datos.
- Servicios de Pólizas y Riesgos → Adaptador CORE → WebLogic → CORE.
- Pólizas → Base de datos: guardar cambio y evento Outbox en una misma transacción.
- Publicador → Base de datos: leer Outbox pendiente.
- Publicador → Broker → Notificaciones → Proveedor de correo/SMS.

Pólizas, Riesgos, Renovación, Adaptador y Publicador son módulos internos de un monolito modular inicial. El contenedor Plataforma de Pólizas agrupa esos módulos; no es un servidor adicional. Base, broker, WebLogic y CORE quedan fuera. Notificaciones puede desplegarse como consumidor independiente. Se delimita la propiedad de los datos por módulo.

## Tres patrones seleccionados

1. **Hexagonal:** dominio dependiente de interfaces, con adaptadores para base, CORE y servicios externos. Permite pruebas aisladas y reemplazar CORE real por mock.
2. **Orientado a eventos:** desacopla las notificaciones del negocio. Outbox transaccional evita perder eventos entre persistencia y publicación; consumidores idempotentes toleran entregas duplicadas.
3. **API Gateway:** concentra acceso, enrutamiento y controles transversales; facilita límites de consumo y versionamiento.

## Consistencia e integración

Toda modificación de estados de pólizas o riesgos consume WebLogic mediante el adaptador. No se informa una operación como sincronizada sin confirmación del CORE.

Se propone registrar la operación con identificador único y sincronización PENDIENTE, invocar WebLogic fuera de una transacción local prolongada y consolidar el cambio y el evento Outbox al confirmar el CORE. Un fallo deja el intento pendiente de resolución; un timeout implica resultado incierto que debe conciliarse. Los estados técnicos de sincronización se separan del estado de negocio.

Se debe acordar con el legado idempotencia y consulta de resultados. Sin esas capacidades no se reenvían modificaciones a ciegas. No existe una transacción atómica implícita entre base local y CORE. Se requiere conciliación si CORE confirma y falla la persistencia local.

## Atributos de calidad

| Aspecto | Estrategia |
| --- | --- |
| Escalabilidad | Instancias sin estado, balanceo, paginación e índices según consultas; consumidores escalables y reclamación de tareas para evitar renovaciones duplicadas. |
| Observabilidad | Logs estructurados, correlación API/CORE/eventos, trazas y métricas de latencia, errores, Outbox y renovaciones pendientes. Evitar datos personales innecesarios. |
| Tolerancia a fallos | Timeouts, circuit breaker, reintentos transitorios limitados con espera exponencial, idempotencia, cola de mensajes fallidos y conciliación. |
| Disponibilidad 24/7 | Réplicas de aplicación, salud, despliegues graduales, base replicada y recuperación probada. Una caída del CORE puede dejar escrituras pendientes. |
| Versionamiento | /api/v1 en producción; nueva versión ante incompatibilidades, transición y esquema de eventos versionado. La práctica conserva las rutas exigidas. |
| Seguridad | TLS, autenticación y autorización por roles, auditoría y secretos externos. La API key requerida es el mínimo de demostración. |

## Implementación práctica

Spring Boot implementará los endpoints exigidos, la API key y la llamada HTTP a `/core-mock/evento` para registrar el intento. No se atribuyen al mock las garantías del CORE real ni se presenta la infraestructura de producción como implementada.
