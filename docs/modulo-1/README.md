# Módulo 1: Diseño de sistema

Plataforma de gestión de pólizas individuales y colectivas de arrendamiento.

## Documentos

- [Arquitectura](arquitectura.md): componentes, tres patrones, integración CORE y atributos de calidad.
- [Modelo de datos](modelo-de-datos.md): entidades, relaciones, reglas y supuestos.

## Diagramas y entrega

Los diagramas de componentes y entidades/relaciones se elaboran en Lucidchart. Sus enlaces de lectura y exportaciones se incorporarán cuando estén disponibles; las exportaciones se guardarán en `diagramas/` y se integrarán en el PDF final. Los enlaces serán complementarios.

Las reglas y aclaraciones permanecen en estos documentos para no sobrecargar los diagramas. Este contenido se consolidará con los demás módulos documentales en un PDF que también incluirá el enlace al repositorio del módulo 2.

## Alcance

El diseño representa una propuesta de producción. El módulo 2 implementará una versión simplificada con Spring Boot, capas controller/service/repository, Poliza y Riesgo, persistencia local, API key y CORE mock HTTP.

Broker, proveedores reales de correo/SMS, renovación programada y alta disponibilidad son elementos del diseño, no funcionalidades ya implementadas.
