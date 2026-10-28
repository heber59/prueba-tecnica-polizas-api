# API de gestión de pólizas

Java 21, Spring Boot, arquitectura hexagonal y H2 en memoria. No requiere instalar una base de datos ni Maven.

## Ejecutar

Requisito: JDK 21 con JAVA_HOME configurado. La primera ejecución necesita internet para descargar Maven y dependencias.

Desde la raíz del repositorio:

```bash
cd api
./mvnw spring-boot:run
```

En PowerShell: `cd api` y luego `.\mvnw.cmd spring-boot:run`.

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [Healthcheck](http://localhost:8080/health)

En Swagger pulsa **Authorize** e ingresa `123456`. La clave se envía como `x-api-key`. Healthcheck y documentación son públicos; el resto requiere la clave.

## Datos iniciales

Cada arranque crea una individual IND-001 con canon 1000000 y una colectiva COL-001 con dos riesgos, de 1500000 y 2000000. Sus vigencias terminan en la fecha de arranque para poder probar renovación inmediatamente. Consulta GET /polizas para obtener los identificadores. H2 se reinicia al detener la aplicación.

## Endpoints

| Método | Ruta | Acción |
| --- | --- | --- |
| GET | /polizas?tipo=INDIVIDUAL&estado=ACTIVA | Listar con filtros opcionales. |
| GET | /polizas/{id}/riesgos | Consultar riesgos. |
| POST | /polizas/{id}/renovar | Renovar con IPC. |
| POST | /polizas/{id}/cancelar | Cancelar póliza y riesgos. |
| POST | /polizas/{id}/riesgos | Agregar riesgo a colectiva. |
| POST | /riesgos/{id}/cancelar | Cancelar riesgo. |
| POST | /core-mock/evento | Registrar intento de actualización CORE. |
| GET | /health | Salud de la aplicación y base local. |

Ejemplos para Bash, usando los IDs iniciales:

```bash
curl -H 'x-api-key: 123456' 'http://localhost:8080/polizas?tipo=INDIVIDUAL&estado=ACTIVA'

curl -X POST http://localhost:8080/polizas/1/renovar \
  -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"ipc":5.2}'

curl -X POST http://localhost:8080/polizas/2/riesgos \
  -H 'x-api-key: 123456' -H 'Content-Type: application/json' \
  -d '{"direccion":"Calle 10","arrendatario":"Ana","beneficiario":"Luis","canonMensual":1500000.00}'

curl -X POST -H 'x-api-key: 123456' http://localhost:8080/polizas/2/cancelar
```

En PowerShell puedes usar Swagger o `curl.exe` adaptando el escape de JSON.

## Reglas

- Individual: exactamente un riesgo. Solo las colectivas admiten agregar riesgos.
- Cancelada: no admite renovación ni nuevos riesgos.
- Cancelar póliza cancela sus riesgos activos; repetir una cancelación no repite la llamada al CORE.
- Solo se renuevan vigencias terminadas, con fecha local America/Bogota. La siguiente comienza al terminar la anterior y dura los mismos meses. Esto rechaza reintentos inmediatos de una vigencia recién renovada; no sustituye una clave de idempotencia ante todos los escenarios.
- IPC entre 0 y 100, expresado como porcentaje. Canon nuevo = canon anterior × (1 + IPC / 100). Prima = canon nuevo × meses. BigDecimal y redondeo HALF_UP a dos decimales.
- En colectivas se actualizan solo riesgos activos y se suman sus cánones y primas. No se reactivan cancelados ni se renueva sin riesgos activos.
- Cancelar el último riesgo no cancela automáticamente la póliza.

## Arquitectura

```text
domain/         Modelos y reglas sin Spring, JPA ni HTTP.
application/    Casos de uso, puertos y servicios.
adapter/in/     Controllers REST, validaciones y errores.
adapter/out/    Persistencia JPA y cliente HTTP CORE.
config/         Ensamblado, seguridad, Swagger y datos iniciales.
```

Las entidades JPA están separadas del dominio; los adaptadores implementan puertos de salida. Los controllers dependen del puerto de entrada.

Toda modificación efectiva invoca por HTTP el mock antes de guardar. Si falla, responde 502 y no guarda cambios locales. El mock registra evento y polizaId en logs. Un éxito remoto seguido de un fallo local requiere conciliación: no se implementa una transacción distribuida.

La exclusión mutua de comandos funciona en una sola instancia. El diseño de producción, con coordinación distribuida, Outbox y notificaciones, se describe en [módulo 1](../docs/modulo-1/README.md). La práctica mantiene solo la vigencia actual en Poliza y participantes como textos en Riesgo; no implementa todo el modelo histórico de producción. Los totales expuestos se calculan sobre riesgos activos actuales, sin contabilidad histórica ni devoluciones.

## Configuración

| Variable | Valor por defecto |
| --- | --- |
| PORT | 8080 |
| API_KEY | 123456 |
| CORE_BASE_URL | http://localhost:puerto configurado |

El adaptador envía la API key al CORE, con timeout de conexión de 2 segundos y lectura de 3 segundos. No reintenta automáticamente modificaciones de resultado incierto. /health no consulta el CORE.

Errores en formato Problem Detail: 400 entrada inválida, 401 clave inválida, 404 recurso inexistente, 409 regla de negocio y 502 integración.

## Pruebas y empaquetado

```bash
./mvnw test
./mvnw package
java -jar target/polizas-api-1.0.0.jar
```

En Windows usa `.\mvnw.cmd`. Las pruebas verifican reglas, cálculo de IPC, cancelación en cascada, filtros, seguridad, Swagger, healthcheck y solicitudes HTTP a un servidor CORE de prueba, incluidos fallos sin persistencia local.
