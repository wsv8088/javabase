package com.wsun.javabase.concurrent.thread;

import lombok.Data;
import org.apache.commons.lang.math.RandomUtils;

import java.util.ArrayDeque;
import java.util.Collections;
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
 *
 * 所以对于这种情况,需要设立一个双方都共享的对象来做协调人,如queue对象,从而实现线程间的通信
 */
public class WrongWaitNotify {

    private static Queue<Product> queue = new ArrayDeque<>();

    public static final int MAX_NUM = 10;


    public static void main(String[] args) {
        Producer producer = new Producer();
        Thread pt = new Thread(new ProducerThread(producer));
        pt.start();

        Consumer consumer = new Consumer();
        Thread ct = new Thread(new ConsumerThread(consumer));
        ct.start();
    }


    /**
     * 模拟生产者服务
     */
    static class Producer {
        public synchronized void product() {
            try {
                if (queue.size() == MAX_NUM) {
                    System.out.println("数据已满,暂停生产!!!");
                    wait();
                }
                Product production = new Product();
                queue.offer(new Product());
                notify();
                System.out.println("生产了一个产品--->" + production + ",通知消费者可以消费!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 模拟消费者服务
     */
    static class Consumer {
        public synchronized void consume() {
            try {
                if (queue.size() == 0) {
                    System.out.println("数据为空,暂停消费!!!");
                    wait();
                }
                Product product = queue.poll();

                notify();
                System.out.println("消费了一个产品--->" + product + ",通知生产者可以生产!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                for (;;) {
                    producer.product();
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(100));
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
                for (;;) {
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
