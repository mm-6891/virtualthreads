package com.pildora.apps.virtualthreads.examples;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class ExecutorServiceClass {
   public static void main(String[] args) {
     final int NTASKS = 100; 
     ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
      for (int i = 0; i < NTASKS; i++) {
         service.submit(() -> {
            long id = Thread.currentThread().threadId(); 
            LockSupport.parkNanos(1_000_000_000);
            System.out.println("Thread id: " + id);
         });
      }
      service.close();
   }
}

