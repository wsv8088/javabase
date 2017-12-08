package com.wsun.javabase.concurrent.lock.semaphore;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Semaphore;

/**
 * -------------用Semaphore来实现银行窗口办理业务----------------
 * 默认初始一个可用的许可数,并用多个线程来模拟多个客户排队等着叫号
 * 初始完信号量之后,一开始的前permits个线程,在acquire都会立即返回,就相当于银行刚开门就可以同时拿到号
 * 之后所有人必须等待,直到某个窗口的客户办理完业务释放许可后,下一个人才可以办理
 */
public class BankBusinessProcess {

    private final static Logger logger = LogManager.getLogger(BankBusinessProcess.class);

    /**
     * 开始营业
     */
    void doBusiness() {
        // 模拟银行只有5个窗口
        BankWindow bankWindow = new BankWindow(5);
        Runnable t = () -> {
            bankWindow.callNumber();
        };
        // 模拟30人办理业务
        for (int i = 0; i < 30; i++) {
            new Thread(t, "线程:" + i).start();
        }

    }

    /**
     * 银行叫号机
     */
    static class BankWindow {

        private Semaphore semaphore;

        public BankWindow(int windowsNum) {
            // 由于银行窗口叫号是先来的先办理业务,为了模拟真实场景,需要使用公平锁的机制,即:FIFO
            semaphore = new Semaphore(windowsNum, true);
        }

        public void callNumber() {
            try {
                semaphore.acquire();
                // 每次抢完票打印出剩余票数
                logger.info("客户:{} 请到窗口办理!", Thread.currentThread().getName());
                // 模拟办理业务需要一定的时间
                Thread.sleep(RandomUtils.nextInt(5000));
                // 办理完业务就空出一个窗口,等着继续叫下一个号
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws UnsupportedEncodingException {
        BankBusinessProcess bankBusinessProcess = new BankBusinessProcess();
        bankBusinessProcess.doBusiness();

    }

}
