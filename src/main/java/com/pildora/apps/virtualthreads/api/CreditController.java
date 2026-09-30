package com.pildora.apps.virtualthreads.api;

import org.springframework.web.bind.annotation.*;

import com.pildora.apps.virtualthreads.domain.Credit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/credits")
public class CreditController {
    private final Map<Long, Credit> credits = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostMapping
    public Credit newCredit(@RequestBody Credit credit) {
        long id = idGen.getAndIncrement();
        credit = credit.setId(id);
        credit = credit.setStatus("PENDING");
        credits.put(id, credit);

        // Simulación de operación costosa de I/O (espera bloqueante de 1000ms)
        try {
            // Simula una llamada a base de datos o servicio externo
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return credit;
    }

    @GetMapping("/{id}")
    public Credit getCredit(@PathVariable Long id) {
        // Simulación de operación costosa de I/O (espera bloqueante de 500ms)
        try {
            // Simula una llamada a base de datos o servicio externo
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return credits.get(id);
    }

    @GetMapping
    public List<Credit> getAllCredits() {
        // Simulación de operación costosa de I/O (espera bloqueante de 800ms)
        try {
            // Simula una llamada a base de datos o servicio externo
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>(credits.values());
    }
}
