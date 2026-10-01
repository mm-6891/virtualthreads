# Benchmark exploratorio: platform vs virtual threads

## Entorno

La API y el generador de carga corrieron en la misma máquina; no son dos hosts independientes.

| Componente | Entorno |
| --- | --- |
| Sistema operativo | macOS 26.6.2, build 25G83, arm64 |
| CPU / memoria | Apple M3, 8 CPU lógicas, 24 GiB |
| Java | OpenJDK 24.0.1 (Oracle) |
| Spring Boot | 3.4.5 |
| Maven | 3.9.10 |
| wrk | 4.2.0, kqueue |

`platform` usa un pool fijo de 8 hilos; `virtual` usa un virtual thread por tarea.

## Pregunta y carga

La serie principal aísla una solicitud bloqueante y de estado estable: `GET /api/credits/1`, que lee el mismo registro y simula 500 ms de espera con `Thread.sleep`. Se siembra un único crédito antes del calentamiento; tras calentar, se limpia y se vuelve a sembrar antes de medir. Durante la carga solo se hacen GET, así que el mapa no crece ni cambia el tamaño de la respuesta.

Protocolo alternado: platform, virtual, virtual, platform. Cada repetición usa un JVM nuevo y ejecuta un calentamiento de 10 s en ese mismo JVM antes de medir. Parámetros idénticos para calentamiento y medición: 2 threads de wrk, 100 conexiones, timeout de 5 s; medición de 30 s.

Comando de medición:

```sh
wrk --latency -t2 -c100 -d30s --timeout 5s \
	-s wrk_scripts/wrk_blocking_get.lua http://127.0.0.1:8080
```

El script imprime `summary.requests` y los errores HTTP mayores de 399 desde el lado cliente. Los timeouts se conservan como métrica separada de `wrk`; no forman parte de la distribución de latencia, que solo describe respuestas completadas.

## Resultados

| Modo / repetición | Completadas | Timeouts | HTTP >399 | req/s | Latencia media | P50 | P75 | P90 | P99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| platform 1 | 472 | 338 | 0 | 15.68 | 1.42 s | 507.66 ms | 2.52 s | 3.54 s | 4.55 s |
| virtual 1 | 5,900 | 0 | 0 | 196.14 | 508.03 ms | 506.69 ms | 510.38 ms | 514.59 ms | 530.57 ms |
| virtual 2 | 5,900 | 0 | 0 | 196.27 | 505.91 ms | 506.02 ms | 507.46 ms | 508.53 ms | 512.69 ms |
| platform 2 | 472 | 343 | 0 | 15.71 | 1.44 s | 505.98 ms | 2.52 s | 4.03 s | 4.54 s |

HTTP >399 es el contador de errores HTTP observado por `wrk`; antes de cada medición se confirmó que el GET de ID 1 respondía 200. Los access logs cerrados muestran status 200 para los GET y 204 para el endpoint de reset.

## Mediana y dispersión

Mediana entre dos repeticiones; el rango mínimo–máximo se ofrece solo como descripción, no como intervalo de confianza.

| Modo | Completadas | Timeouts | req/s | Latencia media | P50 | P75 | P90 | P99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| platform | 472 (472–472) | 340.5 (338–343) | 15.70 (15.68–15.71) | 1.43 s (1.42–1.44) | 506.82 ms (505.98–507.66) | 2.52 s | 3.79 s (3.54–4.03) | 4.55 s (4.54–4.55) |
| virtual | 5,900 (5,900–5,900) | 0 (0–0) | 196.21 (196.14–196.27) | 506.97 ms (505.91–508.03) | 506.36 ms (506.02–506.69) | 508.92 ms (507.46–510.38) | 511.56 ms (508.53–514.59) | 521.63 ms (512.69–530.57) |

En esta carga, virtual completó aproximadamente 12.5 veces más solicitudes por segundo. Platform mantuvo una mediana de latencia cercana a los 506 ms entre sus respuestas completadas, pero tuvo muchos timeouts y una cola que elevó P90/P99. La comparación no debe ocultar esos timeouts.

## Interpretación y límites

- Es evidencia exploratoria para este endpoint bloqueante concreto, no una mejora general para cualquier aplicación.
- `Thread.sleep` representa una espera bloqueante; no mide una base de datos, una llamada HTTP real ni trabajo CPU-bound.
- API y generador comparten CPU y memoria. La carga del generador puede limitar el resultado, sobre todo en el modo que admite más solicitudes.
- Los access logs incluyen calentamiento, resets y respuestas que el servidor termina después de que el cliente haya expirado o cerrado una conexión. Por eso su total no se suma a `summary.requests` ni a los timeouts. Los resultados principales usan los contadores de cliente, que sí clasifican completadas, errores HTTP y expiraciones desde una única fuente.
- Solo hay dos repeticiones por modo. La estabilidad observada es alentadora para este escenario, pero no sustituye más repeticiones, otros niveles de concurrencia ni pruebas con I/O real.

## Artefactos

- Salidas medidas: `stable-platform-1.txt`, `stable-virtual-1.txt`, `stable-virtual-2.txt`, `stable-platform-2.txt`.
- Calentamientos: `stable-platform-1-warmup.txt`, `stable-virtual-1-warmup.txt`, `stable-virtual-2-warmup.txt`, `stable-platform-2-warmup.txt`.
- Carga estable: `../wrk_scripts/wrk_blocking_get.lua`.
- Access logs: `accesslogs/stable-*.2026-10-01.log`.
- `final-*.txt` y los resultados anteriores `platform-*.txt` / `virtual-*.txt` conservan la prueba mixta y sus intentos exploratorios. No son la serie principal porque los POST hacen crecer el mapa y el tamaño de las respuestas del GET de lista.