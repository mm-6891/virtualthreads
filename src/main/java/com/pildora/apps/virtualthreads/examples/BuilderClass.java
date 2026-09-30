package com.pildora.apps.virtualthreads.examples;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.LockSupport;

public class BuilderClass {
    public static void main(String[] args) {        
        Thread.Builder builder = Thread.ofVirtual().name("request-", 1);
        ThreadFactory factory = builder.factory();
        factory.newThread(() -> {
            LockSupport.parkNanos(1_000_000_000);
            System.out.println("Hello from a virtual thread created with a factory!");
        }).start();        

        Thread thread1 = builder.unstarted(() -> {
            LockSupport.parkNanos(1_000_000_000);
            System.out.println("Hello from a virtual thread created as unstarted!");
        });
        thread1.start();

        Thread thread3 = Thread.startVirtualThread(() -> {
            LockSupport.parkNanos(1_000_000_000);
            System.out.println("Hello from a virtual thread created with startVirtualThread!");
        });        
        
        try {
            thread1.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
    }

}