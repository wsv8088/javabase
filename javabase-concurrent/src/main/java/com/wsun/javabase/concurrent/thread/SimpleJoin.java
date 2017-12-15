package com.wsun.javabase.concurrent.thread;

/**
 * 把指定的线程加入到当前线程,可以将两个交替执行的线程合并为顺序执行的线程，相当于线程之前按join调用的顺序串行化执行
 * 比如在线程B中调用了线程A的Join()方法,直到线程A执行完毕后,才会继续执行线程B
 * 调用线程t.join方法的线程,实际是先获得了t的锁,进入临界区后,调用wait()方法,相当于又释放了t的锁
 * 之后等待t的start()方法执行结束后,调用t的线程被唤醒,才可以执行之后的代码
 * 注：在调用t.join()方法时,必须确保t已经是runnable状态,即：已经调用了start()方法,否则join不生效,详见join方法的代码
 * 分析过程：
 * 1、在for循环中,main中首先开启第一个线程t1,并执行它的join方法,那么main线程成功获取到了t1的同步锁,随后
 * join()内部的wait()方法会使进入到临界区的线程,即:main线程释放锁,并进入到等待队列,等待被唤醒
 * 2、之后线程t1开始执行run方法,执行完成后,通知等待t1的线程对象的main线程
 * 重复1、2步骤
 * 3、执行完全部线程后主线程才能继续执行
 */
public class SimpleJoin {
    private static int count = 0;

    public static final int THREAD_NUM = 10;

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_NUM; i++) {
            Thread t = new Timer();
            t.start();
            try {
                System.out.println("线程:" + t.getName() + "加入---");
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("所有线程执行完毕---count:" + count);
    }

    static class Timer extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + "--->" + ++count);
            }
        }
    }

}
