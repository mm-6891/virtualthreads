# Brief para actualizar el artículo del blog

Este documento sirve de traspaso para quien revise el artículo existente. Describe qué cambió desde el benchmark original, qué resultados deben sustituir a los anteriores y qué afirmaciones son defendibles. No es el texto final del artículo.

## Resumen editorial

Actualizar el artículo para presentar una comparación acotada de `PlatformThreads` frente a `VirtualThreads` en un endpoint HTTP bloqueante. La serie principal nueva usa un único GET repetido contra un registro fijo, con calentamiento por JVM, estado constante y orden alternado. No presentar el resultado como una mejora general de virtual threads.

Título sugerido: **Virtual Threads frente a Platform Threads en una API bloqueante: una prueba pequeña**.

Conclusión sugerida: **En esta prueba GET bloqueante, con 100 conexiones concurrentes y un pool platform de 8 hilos, VirtualThreads completó aproximadamente 12,5 veces más solicitudes por segundo. Platform tuvo muchos más timeouts y una cola de latencia larga. Es un resultado exploratorio de esta configuración, no una predicción para cualquier API.**

## Punto de partida

La primera carga usaba [`wrk_mixed.lua`](../wrk_scripts/wrk_mixed.lua), con un patrón aproximado de 50% POST, 40% GET por ID aleatorio y 10% GET de la lista. Los POST agregan créditos al mapa en memoria; por tanto, el estado y el tamaño de la respuesta del GET de lista crecían durante cada ejecución. Además, cada modo podía acumular trabajo pendiente y los conteos del access log no coincidían con las respuestas que `wrk` recibió antes de expirar.

Los resultados mixtos siguen disponibles en `final-*.txt`, `platform-*.txt` y `virtual-*.txt`, junto con sus logs. Mantenerlos como antecedente exploratorio, pero no usarlos como evidencia principal ni mezclarlos con la tabla nueva: la carga y las condiciones de medición son distintas.

## Qué cambió

- Se añadió [`wrk_blocking_get.lua`](../wrk_scripts/wrk_blocking_get.lua), que solicita solo `GET /api/credits/1`. El script informa `summary.requests` y errores HTTP >399 desde el cliente; `wrk` informa los timeouts por separado.
- Se añadió `DELETE /api/credits/_benchmark/reset`, habilitado solo con `app.benchmark.reset-enabled=true`. La prueba de integración verifica que limpia el mapa y restablece el ID.
- Se repitió en orden platform, virtual, virtual, platform. Cada corrida tuvo un JVM propio, calentamiento de 10 s en ese JVM, reset/reseed del registro ID 1 y medición de 30 s. Durante las mediciones no hubo POST.
- Entorno de ambas partes: misma máquina macOS 26.6.2 arm64, Apple M3, 8 CPU lógicas, 24 GiB; OpenJDK 24.0.1, Spring Boot 3.4.5 y `wrk` 4.2.0. Platform usa pool fijo de 8 hilos; virtual crea un virtual thread por tarea.
- Se conservaron salidas completas de cada medición y calentamiento. Los access logs del servidor muestran respuestas HTTP 200 para GET y 204 para reset, pero incluyen calentamiento, controles y respuestas que pueden terminar después de que `wrk` ya haya expirado. No sumar esos registros a los contadores del cliente.

## Resultados principales

Parámetros de medición: `wrk --latency -t2 -c100 -d30s --timeout 5s -s wrk_scripts/wrk_blocking_get.lua http://127.0.0.1:8080`.

| Modo / repetición | Completadas | Timeouts | HTTP >399 | req/s | Media | P50 | P90 | P99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| platform 1 | 472 | 338 | 0 | 15.68 | 1.42 s | 507.66 ms | 3.54 s | 4.55 s |
| virtual 1 | 5,900 | 0 | 0 | 196.14 | 508.03 ms | 506.69 ms | 514.59 ms | 530.57 ms |
| virtual 2 | 5,900 | 0 | 0 | 196.27 | 505.91 ms | 506.02 ms | 508.53 ms | 512.69 ms |
| platform 2 | 472 | 343 | 0 | 15.71 | 1.44 s | 505.98 ms | 4.03 s | 4.54 s |

Medianas con rango mínimo–máximo de solo dos corridas:

- Platform: 472 completadas (472–472), 340.5 timeouts (338–343), 15.70 req/s (15.68–15.71), P50 506.82 ms (505.98–507.66), P90 3.79 s (3.54–4.03), P99 4.55 s (4.54–4.55).
- Virtual: 5,900 completadas (5,900–5,900), 0 timeouts, 196.21 req/s (196.14–196.27), P50 506.36 ms (506.02–506.69), P90 511.56 ms (508.53–514.59), P99 521.63 ms (512.69–530.57).

Los percentiles de latencia representan solicitudes completadas, no los timeouts. En particular, la P50 de platform cercana a 500 ms no describe las solicitudes que expiraron ni la espera de toda la carga ofrecida.

## Lectura recomendada

La diferencia de throughput observada es aproximadamente 12.5x para este endpoint: $196.21 / 15.70$. El resultado concuerda con una carga de espera bloqueante de 500 ms y con platform limitado a ocho hilos. En platform, la mitad de las conexiones concurrentes excede la capacidad del pool y se acumula en cola; en virtual, las esperas bloqueantes no ocupan un hilo platform por solicitud.

Presentarlo como: “En este escenario sintético, VirtualThreads completó más solicitudes por segundo y evitó los timeouts vistos en platform”. Evitar: “Virtual threads hacen cualquier API 12,5 veces más rápida” o usar “12,5x” sin mencionar carga, concurrencia, pool y timeouts.

## Limitaciones que deben aparecer en el artículo

- Es un endpoint de ejemplo con `Thread.sleep(500)`, no una llamada real a base de datos o a otro servicio.
- API y generador de carga compartieron máquina y recursos; no es una prueba con generador independiente.
- Se usaron dos repeticiones por modo. El rango observado no es un intervalo de confianza.
- Platform tuvo 338 y 343 timeouts, frente a cero en las corridas virtuales. Los percentiles excluyen solicitudes que expiraron.
- El pool platform de esta aplicación es fijo de 8 hilos. La conclusión compara esa configuración concreta con virtual threads por tarea.
- Los access logs cuentan respuestas del servidor, no una partición equivalente de completadas y timeouts del cliente. Para resultados principales, usar los contadores de `wrk` y conservar ambos tipos de log como evidencia separada.

## Archivos para revisión

- Informe técnico y protocolo completos: [`README.md`](README.md).
- Mediciones originales: `stable-platform-1.txt`, `stable-virtual-1.txt`, `stable-virtual-2.txt`, `stable-platform-2.txt`.
- Calentamientos originales: `stable-platform-1-warmup.txt`, `stable-virtual-1-warmup.txt`, `stable-virtual-2-warmup.txt`, `stable-platform-2-warmup.txt`.
- Logs HTTP del servidor: `accesslogs/stable-platform-1.2026-10-01.log`, `accesslogs/stable-virtual-1.2026-10-01.log`, `accesslogs/stable-virtual-2.2026-10-01.log`, `accesslogs/stable-platform-2.2026-10-01.log`.
- Implementación de carga: [`wrk_blocking_get.lua`](../wrk_scripts/wrk_blocking_get.lua); carga histórica: [`wrk_mixed.lua`](../wrk_scripts/wrk_mixed.lua).
- Reset de datos para pruebas: [`CreditController.java`](../src/main/java/com/pildora/apps/virtualthreads/api/CreditController.java); verificación: [`VirtualthreadsApplicationTests.java`](../src/test/java/com/pildora/apps/virtualthreads/VirtualthreadsApplicationTests.java).

## Verificaciones hechas

- `mvn -q -Dtest=VirtualthreadsApplicationTests test`: pasó.
- El script Lua se probó contra la API con una carga corta: 72 respuestas completadas, 0 errores HTTP >399 y sin panic.
- `git diff --check`: limpio.