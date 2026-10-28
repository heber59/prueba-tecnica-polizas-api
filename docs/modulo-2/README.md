# MÃ³dulo 2: API de gestiÃ³n de pÃ³lizas

## Objetivo

Implementar la API prÃ¡ctica con arquitectura hexagonal, reglas de negocio claras y ejecuciÃ³n sencilla. El cÃ³digo vivirÃ¡ en [api](../../api/README.md).

Estado: implementaciÃ³n disponible en `api/`. Consulta su README para ejecutar, probar y abrir Swagger.

## TecnologÃ­a

- Java 21, Spring Boot y Maven Wrapper.
- Spring Web, Bean Validation y Spring Data JPA.
- H2 en memoria con datos iniciales para probar individuales y colectivas.
- springdoc-openapi para generar OpenAPI y mostrar Swagger UI.
- Spring Boot Actuator para GET /health.
- JUnit y herramientas de pruebas de Spring Boot.

Las versiones concretas se elegirÃ¡n compatibles al preparar el proyecto.

## Arquitectura hexagonal

```text
api/
â”œâ”€â”€ pom.xml
â”œâ”€â”€ mvnw / mvnw.cmd
â””â”€â”€ src/
    â”œâ”€â”€ main/
    â”‚   â”œâ”€â”€ java/.../polizas/
    â”‚   â”‚   â”œâ”€â”€ domain/
    â”‚   â”‚   â”‚   â”œâ”€â”€ model/
    â”‚   â”‚   â”‚   â””â”€â”€ exception/
    â”‚   â”‚   â”œâ”€â”€ application/
    â”‚   â”‚   â”‚   â”œâ”€â”€ port/in/
    â”‚   â”‚   â”‚   â”œâ”€â”€ port/out/
    â”‚   â”‚   â”‚   â””â”€â”€ service/
    â”‚   â”‚   â”œâ”€â”€ adapter/
    â”‚   â”‚   â”‚   â”œâ”€â”€ in/rest/
    â”‚   â”‚   â”‚   â””â”€â”€ out/
    â”‚   â”‚   â”‚       â”œâ”€â”€ persistence/
    â”‚   â”‚   â”‚       â””â”€â”€ core/
    â”‚   â”‚   â””â”€â”€ config/
    â”‚   â””â”€â”€ resources/
    â””â”€â”€ test/java/
```

Esta estructura organiza la implementaciÃ³n.

- Dominio: Poliza, Riesgo, estados y reglas; sin dependencias de Spring, JPA ni HTTP.
- Puertos de entrada: contratos de los casos de uso de pÃ³lizas y riesgos.
- Servicios de aplicaciÃ³n: implementan casos de uso y coordinan dominio y puertos de salida.
- Puertos de salida: contratos para persistencia y actualizaciÃ³n del CORE.
- Adaptador REST: controllers, DTO, validaciÃ³n de entrada y respuestas de error.
- Adaptador de persistencia: entidades JPA, repositories Spring Data, mapeos manuales e implementaciÃ³n de puertos.
- Adaptador CORE: cliente HTTP que implementa el puerto de integraciÃ³n.
- ConfiguraciÃ³n: ensamblado de dependencias, seguridad y documentaciÃ³n.

Flujo: controller â†’ puerto de entrada / servicio de aplicaciÃ³n â†’ dominio y puertos de salida. Los adaptadores de salida implementan estos Ãºltimos. Se conservan las responsabilidades controller/service/repository exigidas por la prueba.

## Endpoints

| MÃ©todo y ruta | Comportamiento |
| --- | --- |
| GET /polizas?tipo=&estado= | Listar con filtros opcionales y combinables. |
| GET /polizas/{id}/riesgos | Consultar riesgos y sus estados. |
| POST /polizas/{id}/renovar | Aplicar IPC, ajustar vigencia y cambiar a RENOVADA. |
| POST /polizas/{id}/cancelar | Cancelar pÃ³liza y todos sus riesgos activos. |
| POST /polizas/{id}/riesgos | Agregar riesgo Ãºnicamente a colectiva en estado vÃ¡lido. |
| POST /riesgos/{id}/cancelar | Cancelar un riesgo sin eliminar su registro. |
| POST /core-mock/evento | Registrar en logs el intento de actualizaciÃ³n al CORE. |
| GET /health | Informar salud mediante Actuator. |

No se agregan endpoints de creaciÃ³n de pÃ³lizas: los datos iniciales permiten probar las operaciones requeridas.

## Reglas y decisiones

- Una individual tiene exactamente un riesgo; agregar riesgos por API solo se permite en colectivas.
- Una pÃ³liza cancelada no puede renovarse ni recibir riesgos.
- Cancelar pÃ³liza cancela todos sus riesgos activos.
- Renovar recibe un JSON con `ipc`, por ejemplo `{"ipc": 5.2}`: representa 5,2 %.
- Nuevo canon = canon anterior Ã— (1 + IPC / 100).
- Prima = nuevo canon Ã— meses de vigencia. En colectivas se calcula por riesgo activo y se suman los importes.
- Usar BigDecimal, dos decimales y redondeo HALF_UP.
- RenovaciÃ³n mantiene la duraciÃ³n inicial y comienza al finalizar la vigencia anterior; los riesgos cancelados no se reactivan.
- Estados de pÃ³liza: ACTIVA, RENOVADA y CANCELADA. Estados de riesgo: ACTIVO y CANCELADO.
- Cancelar algo ya cancelado devuelve su estado sin repetir la actualizaciÃ³n al CORE.
- Cancelar el Ãºltimo riesgo no cancela automÃ¡ticamente la pÃ³liza: supuesto de la prÃ¡ctica.
- No se exige Idempotency-Key en esta versiÃ³n. Solo se permite renovar una vigencia terminada; las nuevas fechas impiden repetir inmediatamente la renovaciÃ³n. No equivale a idempotencia completa.

## CORE mock

Las operaciones que modifican estados consumen el mock mediante HTTP real, a travÃ©s del adaptador. El mock puede alojarse en la misma aplicaciÃ³n, con URL configurable.

```json
{
  "evento": "ACTUALIZACION",
  "polizaId": 555
}
```

Primero se validan las reglas. Si el CORE falla, no se confirman cambios locales y se devuelve error de integraciÃ³n. La llamada remota no participa de una transacciÃ³n atÃ³mica con H2; si CORE confirma y falla la persistencia local, la prÃ¡ctica no implementa conciliaciÃ³n automÃ¡tica. El mecanismo completo de producciÃ³n pertenece al mÃ³dulo 1.

## Seguridad y Swagger

Los endpoints de negocio y el mock exigen `x-api-key: 123456`. El adaptador tambiÃ©n envÃ­a esa clave al mock.

Swagger UI y su documento OpenAPI se pueden consultar sin clave. Las operaciones protegidas se ejecutan despuÃ©s de ingresar la clave en Authorize. Se documentan cuerpos, ejemplos, respuestas y errores.

GET /health serÃ¡ pÃºblico, sin detalles internos. Respuesta sana: `{"status":"UP"}`; Actuator devolverÃ¡ el cÃ³digo HTTP correspondiente al estado de salud. No se expondrÃ¡n otros endpoints de administraciÃ³n innecesarios.

## Errores

| CÃ³digo | Uso |
| --- | --- |
| 400 | Datos, filtros o IPC invÃ¡lidos. |
| 401 | API key ausente o incorrecta. |
| 404 | PÃ³liza o riesgo inexistente. |
| 409 | OperaciÃ³n incompatible con tipo o estado. |
| 502 | Fallo al consumir CORE. |

## Pruebas y ejecuciÃ³n

- Pruebas del dominio para IPC, restricciones y cancelaciÃ³n en cascada.
- Pruebas de API para filtros, validaciones, estados HTTP y API key.
- Pruebas del adaptador para comprobar la solicitud HTTP al CORE y el manejo de fallos.
- ComprobaciÃ³n de /health y documentaciÃ³n OpenAPI.
- Datos iniciales y ejemplos reproducibles en Swagger.

El README de la API incluye requisitos, arranque con Maven Wrapper, Swagger, healthcheck y ejecuciÃ³n de pruebas.

## Secuencia de implementación

1. Cerrar DTO, respuestas y criterio de renovaciÃ³n por vigencia.
2. Preparar Spring Boot y configuraciÃ³n.
3. Implementar dominio, puertos y adaptadores de persistencia.
4. Implementar casos de uso y REST.
5. Incorporar CORE HTTP y API key.
6. Configurar Swagger y /health.
7. Ejecutar pruebas y completar instrucciones.

## Alcance frente al mÃ³dulo 1

La prÃ¡ctica usa Poliza y Riesgo con una representaciÃ³n simplificada de vigencias y participantes. El mÃ³dulo 1 conserva el modelo completo de producciÃ³n. Broker, Outbox, Gateway desplegado, proveedores de notificaciones y alta disponibilidad no se implementan en esta prÃ¡ctica.
