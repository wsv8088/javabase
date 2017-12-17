package com.wsun.javabase.concurrent.thread;

import lombok.Data;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者/消费者场景--错误的案例
 * 1、线程pt启动,run方法中调用了producer对象p的product同步方法,获取到锁后,进入到临界区,之后调用了p的wait()方法
 * pt释放了锁,并进入等待状态,等待其他线程在一定条件下调用p的notify方法唤醒自己
 * 2、线程ct同理
 * 3、因为pt和ct在调用生产和消费的同步方法后,分别调用了p和c的wait()方法,释放了锁并等待唤醒,
 * 4、之后并没有其他线程在同步块中调用共享对象p或c的notify方法,并且生产者和消费者之间也没有一个共同的协调人(共享对象),
 * 相当于p只被pt监视,c只被ct监视,这样一来,无论是pt还是ct,一旦进入等待状态,就没有机会被其他线程唤醒
 * <p>
 * 所以对于这种情况,需要设立一个双方都共享的对象来做协调人,如queue对象,从而实现线程间的通信
 */
public class RightWaitNotify {
    private final static Logger LOGGER = LogManager.getLogger(RightWaitNotify.class);

    private static Queue<Product> queue = new ArrayDeque<>();

    public static final int MAX_NUM = 10;

    public static void main(String[] args) {

        Producer producer = new Producer();
        Thread pt = new Thread(new ProducerThread(producer));
        pt.start();
        pt = new Thread(new ProducerThread(producer));
        pt.start();

        Consumer consumer = new Consumer();
        Thread ct = new Thread(new ConsumerThread(consumer));
        ct.start();
    }


    /**
     * 模拟生产者服务
     */
    static class Producer {
        public void product() {
            synchronized (queue) {
                try {
                    /**
                     * 此处需要使用while而不是if
                     * 在只有一个生产者和一个消费者的情况下,使用if是没问题的,但是在同时有多个生产者和消费者的情况下,会出现问题
                     * 原因是：
                     * 在进入到此临界区的线程,发现队列已满,调用了缓存区对象queue的wait()方法后,当前线程释放了锁,并进入到等待队列中
                     * 这样的话,如果当前线程被唤醒,就不会再判断队列是否已满,直接往队列中增加元素,但是这之前
                     * 由于有多个生产者线程,处理等待中的生产者不只一个,wait()后面的逻辑可能已经被其他优先执行的生产者线程执行过了
                     * 导致queue的数量超出max的限制
                     *
                     * 可以手动改为if测试下效果,结果是分分钟queue.size()就超出了MAX_NUM
                     */
                    while (queue.size() == MAX_NUM) {
                        LOGGER.info("数据已满,暂停生产!!!");
                        queue.wait();
                    }
                    Product production = new Product();
                    queue.offer(new Product());
                    queue.notify();

                    LOGGER.info("生产了一个产品--->{},当前队列共:{}个元素,通知消费者可以消费!!!", production, queue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 模拟消费者服务
     */
    static class Consumer {
        public void consume() {
            synchronized (queue) {
                try {
                    /**
                     * 此处需要使用while而不是if,原因可参考生产者
                     */
                    while (queue.size() == 0) {
                        System.out.println("数据为空,暂停消费!!!");
                        queue.wait();
                    }
                    Product product = queue.poll();

                    queue.notify();
                    LOGGER.info("消费了一个产品--->{},当前队列共:{}个元素,通知生产者可以生产!!!", product, queue.size());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    /**
     * 模拟消费者线程,随机生产一个
     */
    static class ProducerThread implements Runnable {
        private Producer producer;

        public ProducerThread(Producer producer) {
            this.producer = producer;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    producer.product();
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(10));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class ConsumerThread implements Runnable {
        private Consumer consumer;

        public ConsumerThread(Consumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try {
                for (; ; ) {
                    consumer.consume();
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(100));

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 模拟产品对象
     */
    @Data
    static class Product {
        private static AtomicInteger idGenerator = new AtomicInteger(0);

        private int productID;

        public Product() {
            this.productID = idGenerator.incrementAndGet();
        }
    }

}
