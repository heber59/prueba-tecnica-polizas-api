# Módulo 5: Liderazgo técnico y gestión

## 1. ¿Cuáles serían tus cinco prioridades en las primeras dos semanas?

1. **Estabilizar los servicios críticos.** Revisaría los diez incidentes del último mes, priorizando los que siguen abiertos, su impacto y las causas recurrentes. Delegaría responsables entre los desarrolladores con más experiencia, con seguimiento y apoyo técnico. La atención de incidentes también debe dejar acciones para evitar que se repitan.

2. **Definir un flujo de trabajo y estándares mínimos.** Establecería criterios claros de desarrollo, pruebas, revisión y cierre de historias. El equipo debe conocer qué significa una tarea terminada y cómo llevarla hasta producción. Empezaría con acuerdos breves y aplicables, sin frenar la atención de incidentes mientras se documentan.

3. **Acompañar a los dos juniors.** De los ocho desarrolladores, asignaría a dos de los seis con mayor experiencia como mentores, uno por junior. Comenzarían con trabajo en pareja para conocer el código y el flujo del equipo. Durante la primera semana tendría un espacio individual con cada junior para escuchar sus dificultades y acordar un plan de mejora con objetivos concretos y revisiones semanales.

4. **Acordar el alcance de la funcionalidad y atacar la deuda que la bloquea.** Desde la primera semana dividiría la funcionalidad en entregas pequeñas con criterios de aceptación. Conforme disminuyan los incidentes, movería capacidad hacia su desarrollo y hacia la deuda técnica que más afecte la estabilidad o esa entrega. El dato de 40 % de deuda requiere identificar sus riesgos; no intentaría resolverla toda en tres semanas.

5. **Llegar a la tercera semana con una entrega viable.** Al cerrar la segunda semana revisaría avances, riesgos y alcance con negocio. La última semana se enfocaría en terminar, probar y desplegar lo comprometido, manteniendo cobertura de incidentes. La funcionalidad debe comenzar antes de esa semana para que quede tiempo de validación y corrección.

## 2. ¿Cómo organizarías al equipo para mejorar velocidad y calidad?

Organizaría el trabajo según la experiencia de cada persona, la prioridad del negocio y las necesidades de aprendizaje. Empezaría escuchando al equipo para conocer sus fortalezas y las áreas en las que quiere crecer.

Como distribución inicial, dos desarrolladores experimentados atenderían estabilización; otros dos desarrollarían la funcionalidad junto con los dos juniors, en dos parejas; y los dos restantes avanzarían en la funcionalidad y la deuda que la bloquea. Ajustaría esta distribución según la carga de incidentes, sin convertirla en grupos aislados. Como líder, apoyaría las decisiones técnicas, los bloqueos y la coordinación con negocio.

Cada junior tendría tareas que pueda completar con acompañamiento y una o dos tareas acotadas orientadas a lo que quiera mejorar. Reservaría tiempo real de los mentores para el pair programming; acompañar también consume capacidad y debe considerarse al comprometer entregas.

Las revisiones de código las realizaría alguien con experiencia en el área afectada. Para cambios críticos buscaría un revisor distinto de la pareja que los desarrolló. También rotaría la participación para compartir conocimiento y evitar depender siempre de una sola persona. Mantendría pocas tareas en curso y cambios pequeños para facilitar la revisión y detectar problemas antes.

## 3. ¿Qué métricas implementarías para evaluar el desempeño del área?

Evaluaría si el equipo cumple objetivos y entrega producto útil con estabilidad. Compararía la tendencia entre sprints, considerando cambios de alcance, complejidad e incidentes; una caída serviría para investigar obstáculos y acordar mejoras.

| Métrica                                           | Qué permite evaluar                                                                                                             |
| ------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| Cumplimiento del objetivo del sprint              | Si entregamos el resultado acordado, con sus criterios de aceptación, y por qué se desvía cuando no se cumple.                  |
| Tiempo de ciclo                                   | Cuánto tarda una tarea desde que comienza hasta que queda entregada y dónde se acumulan esperas o bloqueos.                     |
| Incidentes críticos y reincidencia                | Si mejora la estabilidad y si las correcciones resuelven causas recurrentes.                                                    |
| Tiempo de recuperación                            | Qué tan rápido recuperamos el servicio cuando ocurre un incidente.                                                              |
| Cambios que provocan fallos o requieren reversión | Si estamos entregando con calidad o trasladando problemas a producción.                                                         |
| Resultado de la funcionalidad                     | Si el producto cumple el beneficio acordado: por ejemplo, menor tiempo de operación, menos errores o adopción por los usuarios. |

Revisaría estas métricas con el equipo y negocio, usando una línea base y metas realistas. El cumplimiento de entregas debe leerse junto con calidad y resultado: entregar más tareas no sirve si aumentan los incidentes o no se resuelve la necesidad del usuario.

## 4. ¿Qué prácticas técnicas establecerías como obligatorias?

- **Trabajo con enfoque AI-first y responsabilidad del desarrollador.** Promovería el uso de herramientas de IA aprobadas para explorar soluciones, escribir pruebas y apoyar tareas repetitivas. Cada desarrollador debe revisar, comprender y poder explicar lo que entrega, incluidas sus decisiones y limitaciones. No se incorporan secretos ni datos sensibles en herramientas no autorizadas.
- **Estándares claros y verificables.** Convenciones de código, arquitectura y manejo de errores acordadas por el equipo, con formato y análisis automatizados cuando sea posible.
- **Revisión mediante pull request o merge request.** Ningún cambio llega directamente a la rama principal. El autor explica qué resuelve y cómo lo verificó; un revisor con experiencia comprueba la implementación. Las observaciones y decisiones quedan en comentarios del PR/MR y los puntos bloqueantes se resuelven antes de fusionar.
- **Pruebas y validación funcional.** Las pruebas automáticas deben cubrir las reglas y riesgos del cambio, y ejecutarse en integración continua. Durante la revisión se valida el comportamiento contra los criterios de aceptación, especialmente en flujos críticos.
- **Cierre de historias con evidencia.** La aprobación del código es parte del proceso. Una historia se cierra cuando cumple los criterios de aceptación, tiene las verificaciones requeridas y está disponible en el entorno acordado en la definición de terminado.
- **Despliegues controlados y observables.** Los cambios deben dejar logs y señales útiles para detectar fallos, además de una forma de revertir o mitigar problemas. Los incidentes críticos generan una revisión sin culpas y acciones con responsables.

## 5. ¿Cómo gestionarías la presión del negocio sin comprometer la calidad?

Primero evaluaría con el equipo si el alcance cabe realmente en las tres semanas, considerando incidentes, dependencias, pruebas y acompañamiento de los juniors. Con esa información acordaría metas y fechas claras con negocio, haciendo visibles los supuestos y riesgos desde el inicio.

Hay condiciones mínimas que mantendría: seguridad, integridad de los datos, pruebas de los flujos críticos y una salida controlada a producción. Si alcance y plazo no permiten cumplirlas, presentaría alternativas concretas: entregar primero un alcance menor que aporte valor, dividir la funcionalidad en etapas o ajustar la fecha. Añadir personas no garantiza una solución inmediata porque también requiere coordinación y contexto.

Mantendría seguimiento frecuente con demostraciones y avances verificables. Si aparece una desviación, la comunicaría cuando todavía podemos actuar, junto con su impacto y una propuesta. El compromiso sería entregar un resultado útil y confiable dentro de una capacidad realista, evitando prometer una cantidad de trabajo que el equipo no puede sostener.
