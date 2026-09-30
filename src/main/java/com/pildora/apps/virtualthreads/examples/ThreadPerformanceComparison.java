package com.pildora.apps.virtualthreads.examples;

import java.util.ArrayList;
import java.util.List;

public class ThreadPerformanceComparison {
    private static final int TASKS = 10000;
    private static final int SLEEP_MILLIS = 10;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Comparando rendimiento: Virtual Threads vs Platform Threads");
        System.out.println("Tareas: " + TASKS + ", Sleep por tarea: " + SLEEP_MILLIS + "ms");

        long platformTime = runWithPlatformThreads();
        long virtualTime = runWithVirtualThreads();

        System.out.println("\nTiempo total Platform Threads: " + platformTime + " ms");
        System.out.println("Tiempo total Virtual Threads:  " + virtualTime + " ms");
    }

    private static long runWithPlatformThreads() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < TASKS; i++) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        return System.currentTimeMillis() - start;
    }

    private static long runWithVirtualThreads() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < TASKS; i++) {
            Thread t = Thread.ofVirtual().unstarted(() -> {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
        return System.currentTimeMillis() - start;
    }
}
