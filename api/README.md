## Ejecutar

Desde la raíz del repositorio:

```bash
cd api
./mvnw spring-boot:run
```

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [Healthcheck](http://localhost:8080/health)

En Swagger pulsa **Authorize** e ingresa `123456`. La clave se envía como `x-api-key`. Healthcheck y documentación son públicos; el resto requiere la clave.

## Datos iniciales

Cada arranque crea una individual IND-001 con canon 1000000 y una colectiva COL-001 con dos riesgos, de 1500000 y 2000000. Sus vigencias terminan en la fecha de arranque para poder probar renovación inmediatamente. Consulta GET /polizas para obtener los identificadores. H2 se reinicia al detener la aplicación.

## Endpoints

| Método | Ruta                                   | Acción                                   |
| ------ | -------------------------------------- | ---------------------------------------- |
| GET    | /polizas?tipo=INDIVIDUAL&estado=ACTIVA | Listar con filtros opcionales.           |
| GET    | /polizas/{id}/riesgos                  | Consultar riesgos.                       |
| POST   | /polizas/{id}/renovar                  | Renovar con IPC.                         |
| POST   | /polizas/{id}/cancelar                 | Cancelar póliza y riesgos.               |
| POST   | /polizas/{id}/riesgos                  | Agregar riesgo a colectiva.              |
| POST   | /riesgos/{id}/cancelar                 | Cancelar riesgo.                         |
| POST   | /core-mock/evento                      | Registrar intento de actualización CORE. |
| GET    | /health                                | Salud de la aplicación y base local.     |

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

## Configuración

| Variable | Valor por defecto |
| -------- | ----------------- |
| PORT     | 8080              |
| API_KEY  | 123456            |

## Pruebas y empaquetado

```bash
./mvnw test
./mvnw package
java -jar target/polizas-api-1.0.0.jar
```
