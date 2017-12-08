package com.wsun.javabase.concurrent.guava;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.*;

/**
 * google限流器学习
 */
public class GuavaRateLimiter {

    /**
     * RateLimiter create(double permitsPerSecond, long warmupPeriod, TimeUnit unit)
     * 创建一个限速器
     * @param permitsPerSecond：每秒允许发送的请求数,如果设置为t,则相当于1/t秒会产生一个令牌
     * @param warmupPeriod：预热时间,产生令牌的速率会根据这个参数以一定规则从一个起始速率稳步上升，超出预热期时，产生速率到达1/t秒这个峰值
     * 以本demo为例,刚运行的时候获取一个令牌需要0.29s,差不多5s以后,到达0.1s
     * 调用create方法时,桶中就开始产生令牌,所以刚创建完就开始获取,一般情况下,首个令牌是不需要等待的,之后的会均匀的等待1/t秒才获取到令牌
     * @param TimeUnit unit：预热的时间单位
     */
//    RateLimiter rateLimiter = RateLimiter.create(10, 5L, TimeUnit.SECONDS);

    RateLimiter rateLimiter = RateLimiter.create(10);

    public static void main(String[] args) throws InterruptedException {
        GuavaRateLimiter limiter = new GuavaRateLimiter();
        limiter.singleThreadAcquire();
//        limiter.multiThreadAcquire();
//        limiter.singleThreadTryAcquire();
//        limiter.singleThreadTryAcquire(150);
    }

    /**
     * acquire()返回值：获取一个许可(令牌)的阻塞时长,单位是微秒
     */

    public void singleThreadAcquire() {
        for (int i = 0; i < 100; i++) {
            double elapse = rateLimiter.acquire();
            System.out.println(Thread.currentThread().getName() + "--->" + elapse);
        }
    }

    /**
     * 多线程的场景下,并发线程的数量直接影响许可时等待的时间
     * 例如：permitsPerSecond被设置为10.0,那么当10个线程并行获取许可的情况下,
     * 获取一个许可的阻塞时长会从单线程的0.1s增大到0.1s * 10(线程数)
     * 相当于：虽然0.1s会产生一个令牌,但每放出一个令牌,就会同时有10个人在等着拿这个令牌
     * 平均到一个人身上拿到的概念只有1/10,所以时间上也相应的增长,所以也可以看出这个acquire会按线程数公平的等待
     */
    public void multiThreadAcquire() {
        ExecutorService service = new ThreadPoolExecutor(0, 100,
                60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        GuavaRateLimiter limiter = new GuavaRateLimiter();
        for (int i = 0; i < 100; i++) {
            service.submit(() -> limiter.singleThreadAcquire());
        }

    }


    /**
     * 如果请求到了许可,返回true,否则返回false
     * 因为在for体里去请求许可,每次调用tryAcquire的间隔时间很短
     * 本例的100次循环即使都执行完,也没超过产生第二个令牌的时间,因为就第一个成功,其他均失败
     */
    public void singleThreadTryAcquire() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (rateLimiter.tryAcquire()) {
                System.out.println("success!!!");
            } else {
                System.out.println("failed!!!");
            }
        }
    }

    /**
     * 指定timeout,如果timeout>产生令牌的速率,tryAcquire都会返回true
     * @param timeout
     * @throws InterruptedException
     */
    public void singleThreadTryAcquire(long timeout) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            if (rateLimiter.tryAcquire(1, timeout, TimeUnit.MILLISECONDS)) {
                System.out.println("success!!!");
            } else {
                System.out.println("failed!!!");
            }
        }
    }


}
