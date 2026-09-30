package com.pildora.apps.virtualthreads.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;

public class VirtualThreadsExecutorsStructuredExample {
    public static void main(String[] args) throws Exception {
        System.out.println("Ejemplo con ExecutorService de Virtual Threads:");
        executorServiceExample();
        System.out.println("\nEjemplo con StructuredTaskScope (Structured Concurrency):");
        structuredConcurrencyExample();
    }

    private static void executorServiceExample() throws InterruptedException, ExecutionException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future1 = executor.submit(() -> {
                Thread.sleep(500);
                return "Resultado tarea 1 (virtual thread)";
            });
            Future<String> future2 = executor.submit(() -> {
                Thread.sleep(300);
                return "Resultado tarea 2 (virtual thread)";
            });
            System.out.println(future1.get());
            System.out.println(future2.get());
        }
    }

    private static void structuredConcurrencyExample() throws Exception {
        // Simulación de Structured Concurrency usando ExecutorService y Future
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<String> task1 = () -> {
                Thread.sleep(400);
                return "Tarea estructurada 1 (virtual thread)";
            };
            Callable<String> task2 = () -> {
                Thread.sleep(200);
                return "Tarea estructurada 2 (virtual thread)";
            };
            List<Future<String>> results = executor.invokeAll(List.of(task1, task2));
            for (Future<String> result : results) {
                System.out.println(result.get());
            }
        }
    }
}
