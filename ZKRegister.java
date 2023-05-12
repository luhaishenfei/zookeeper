package com.hotwind.synchronizetabledata.util;

import org.apache.zookeeper.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

@Component
@Order(value = 1)
public class ZKRegister implements ApplicationRunner {



    @Override
    public void run(ApplicationArguments args) throws Exception {
        register();
    }

    @Resource
    Environment environment;

    public void register() {
        String port = environment.getProperty("server.port");
        String webApp = environment.getProperty("spring.application.name");
        String zkIp = environment.getProperty("zk.ip");

        String serviceIp = null;
        try {
            serviceIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper =
                    new ZooKeeper(zkIp + ":2181",
                            4000, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (Event.KeeperState.SyncConnected == event.getState()) {
                                //如果收到了服务端的响应事件，连接成功
                                countDownLatch.countDown();
                            }
                        }
                    });

            countDownLatch.await();
            //CONNECTED
            System.out.println(zooKeeper.getState());
            //检查服务是否已经注册
            if (zooKeeper.exists("/" + webApp, false) == null) {
                zooKeeper.create("/" + webApp, serviceIp.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.CONTAINER);
            }
            //添加节点
            zooKeeper.create("/" + webApp + "/" + serviceIp, port.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
