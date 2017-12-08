package com.wsun.javabase.concurrent.lock.semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Semaphore;

/**
 * -------------用Semaphore来实现抢票----------------
 * 初始化一个semaphore要指定其初始的许可个数
 * acquire()方法会默认获得一个许可,也可以调用它的acquire(int permits)方法指定要获取许可的个数,如果semaphore中有可用的许可将会立即返回
 * 否则,直到调用release方法,释放一个许可后才可获取许可
 * 与synchronized最主要的区别是:
 * 1、同步块为了保护资源,同一时间内只允许一个线程进入临界区,这样确保在并发访问同一处共享资源时出现数据不一致的问题
 * 2、信号量主要应对同一类资源的多个副本的并发访问的场景：如多人抢电影票,只要电影票充足,就允许多人同时抢票
 */
public class GrabTicket {

    private final static Logger logger = LogManager.getLogger(GrabTicket.class);

    /**
     * 抢票
     */
    void grabTicket() {
        Ticket ticket = new Ticket(50);
        Runnable t = () -> {
            ticket.getTicket();
        };
        // 模拟30人同时抢票
        for (int i = 0; i < 30; i++) {
            new Thread(t, "线程:" + i).start();
        }

    }


    static class Ticket {
        private Semaphore semaphore;
        Ticket(int qty) {
            semaphore = new Semaphore(qty);
        }
        public void getTicket() {
            try {
                semaphore.acquire();
                // 每次抢完票打印出剩余票数
                logger.info("{} 成功抢到了一张电影票!", Thread.currentThread().getName());
                showTicketQty();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void showTicketQty() {
            logger.info("剩余:{}张票", semaphore.availablePermits());
        }
    }

    public static void main(String[] args) {
        new GrabTicket().grabTicket();
    }
}
