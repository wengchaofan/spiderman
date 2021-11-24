package test;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class CasTest {

    private static final Unsafe unsafe = getUnsafe();
    public volatile int num = 0 ;

    private static long offset;

    static {
        try {
            offset = unsafe.objectFieldOffset(CasTest.class.getDeclaredField("num"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private int getAndInt(int n){
        return unsafe.getAndAddInt(this,offset,n);
    }

    private boolean compareAndSwapInt(int expect,int update){
        return unsafe.compareAndSwapInt(this, offset, expect, update);
    }

    public static void main(String[] args) throws InterruptedException {
        addNumByCas();

        addNumByLock();

    }

    private static void addNumByLock() throws InterruptedException {

        long t1 = System.currentTimeMillis();
        CasTest casTest = new CasTest();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ReentrantLock reentrantLock = new ReentrantLock();
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10000000;  j++) {
                    reentrantLock.lock();
                    casTest.num ++;
                    reentrantLock.unlock();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("Lock计算结果: "+ casTest.num + ",计算耗时：" + (System.currentTimeMillis() -t1) + "ms");

    }

    private static void addNumByCas() throws InterruptedException {
        long t1 = System.currentTimeMillis();
        CasTest casTest = new CasTest();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10000000; j++) {
                    casTest.getAndInt(j);
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        System.out.println("CAS计算结果: "+ casTest.num + ",计算耗时：" + (System.currentTimeMillis() -t1) + "ms");
    }

    private static Unsafe getUnsafe() {
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe)theUnsafe.get(null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

}
