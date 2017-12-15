package com.wsun.javabase.concurrent.thread;

import lombok.Data;
import org.apache.commons.lang.math.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者/消费者场景--错误的案例
 * 1、线程pt和ct在调用wait()方法之前,获取到相应生产或消费类对象的锁,之后在一定的条件下调用producer/consumer的wait()方法
 * 2、因为拿到锁的对象,只有一个线程,并且线程之间wait和notify并没有一个共同的协调人(共享对象),这样wait被调用后
 * 就没有其他线程在调用这个对象的notify方法来唤醒之前wait的线程,就造成一直等待
 */
public class WrongWaitNotify {

    private static List<Product> list = new ArrayList<>();

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
                if (list.size() == MAX_NUM) {
                    System.out.println("数据已满,暂停生产!!!");
                    wait();
                }
                Product production = new Product();
                list.add(production);
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
                if (list.size() == 0) {
                    System.out.println("数据为空,暂停消费!!!");
                    wait();
                }
                Product production = list.remove(0);

                notify();
                System.out.println("消费了一个产品--->" + production + ",通知生产者可以生产!!!");
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
