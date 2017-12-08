package com.wsun.javabase.concurrent.guava;

import com.google.common.util.concurrent.*;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GuavaFuture {
    private final static Logger LOGGER = LogManager.getLogger(GuavaFuture.class);

    ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    private UserService userService = new UserServiceImpl();


    public static void main(String[] args) {
        GuavaFuture future = new GuavaFuture();
//        future.futureGet();
//        future.futureAddListener();
//        future.futureCallback();
    }

    private void futureGet() {

        List<ListenableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ListenableFuture<String> future = executorService.submit(() ->
                userService.getUserNameByID(RandomUtils.nextLong())
            );

            futures.add(future);
        }

        futures.stream().forEach(future -> {
            try {
                LOGGER.info(future.get());
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });

    }

    /**
     * Future得到结果后的通知--实现方式：增加监听器
     */
    private void futureAddListener() {

        List<ListenableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ListenableFuture<String> future = executorService.submit(() ->
                userService.getUserNameByID(RandomUtils.nextLong())
            );
            future.addListener(() -> {
                try {
                    LOGGER.info("监控到有结果返回:" + future.get());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }, executorService);
            futures.add(future);
        }

    }


    /**
     * Future得到结果后的通知--实现方式：增加回调函数
     */
    private void futureCallback() {

        List<ListenableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ListenableFuture<String> future = executorService.submit(() ->
                    userService.getUserNameByID(RandomUtils.nextLong())
            );
            Futures.addCallback(future, new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    LOGGER.info("成功获取结果:{}", result);
                }

                @Override
                public void onFailure(Throwable t) {
                    LOGGER.error("获取结果失败");
                }
            });
        }

    }



    interface UserService {
        String getUserNameByID(Long id);
    }

    static class UserServiceImpl implements UserService {

        @Override
        public String getUserNameByID(Long id) {
            try {
                Thread.sleep(2000L);
            } catch (Exception e) {
                LOGGER.error(e);
            }
            return Thread.currentThread().getName() + ":" + id;
        }
    }



}
