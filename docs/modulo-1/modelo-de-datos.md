# Modelo de datos de pólizas y riesgos

## Entidades

| Entidad         | Atributos principales                                                                                                                   |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| TERCERO         | id PK, tipo_persona, tipo_documento, numero_documento, nombre_razon_social, email, telefono                                             |
| POLIZA          | id PK, numero único, tipo, estado, tomador_id FK, fecha_creacion                                                                        |
| VIGENCIA_POLIZA | id PK, poliza_id FK, fecha_inicio, fecha_fin, duracion_meses, ipc_aplicado, prima_total, vigencia_anterior_id FK opcional y única       |
| INMUEBLE        | id PK, direccion, ciudad, identificador_inmueble                                                                                        |
| RIESGO          | id PK, vigencia_id FK, inmueble_id FK, arrendatario_id FK, beneficiario_id FK, estado, canon_mensual, prima, fecha_cancelacion opcional |

## Relaciones y cardinalidades

- Un tercero tiene cero o muchas pólizas como tomador; cada póliza tiene un tomador.
- Una póliza emitida tiene una o muchas vigencias; cada vigencia pertenece a una póliza.
- Una vigencia emitida contiene uno o muchos registros de riesgo; cada riesgo pertenece a una vigencia.
- Un inmueble puede aparecer en cero o muchos riesgos a lo largo del tiempo; cada riesgo corresponde a un inmueble.
- Un tercero puede ser arrendatario/asegurado de cero o muchos riesgos; cada riesgo tiene un arrendatario.
- Un tercero puede ser arrendador/beneficiario de cero o muchos riesgos; cada riesgo tiene un beneficiario.
- Una vigencia tiene cero o una anterior y cero o una sucesora. No se permiten ciclos ni referencias entre pólizas distintas.

## Reglas de negocio

1. Individual: exactamente un riesgo por vigencia; tomador igual al arrendatario/asegurado.
2. Colectiva: uno o más riesgos al emitirse; tomador inmobiliaria o administración de copropiedad.
3. Beneficiario de cada riesgo: su arrendador.
4. Agregar riesgos mediante la API solo se permite en colectivas y estados que admitan modificaciones.
5. Cancelar póliza cancela todos sus riesgos activos, conservando el historial.
6. Una póliza cancelada no puede renovarse.
7. Renovar conserva la póliza y crea una vigencia por la duración inicial, con nuevos registros para los riesgos que continúan. No sobrescribe la anterior.
8. En la práctica, renovar cambia el estado a RENOVADA; la elegibilidad también considera la vigencia para evitar procesar dos veces un periodo.
9. Toda modificación de estados consume la integración CORE.
