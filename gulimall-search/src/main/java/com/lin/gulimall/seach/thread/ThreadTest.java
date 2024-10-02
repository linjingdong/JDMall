package com.lin.gulimall.seach.thread;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description TODO
 * @Date 2024/6/26 16:23
 * @Author Lin
 * @Version 1.0
 */
public class ThreadTest {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main ... start ...");
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor).handle((resp, exec) -> {
            if (resp != null) {
                return resp * 2;
            }
            if (exec != null) {
                return 0;
            }
            return 0;
        });
        Integer unused = future.get();
        System.out.println("main ... end ..." + unused);
    }
}
