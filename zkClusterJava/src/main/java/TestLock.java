//import com.mxn.zookeeper.config.ZKUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @program: mxnzookeeper
 * @ClassName TestLock
 * @description:
 * @author: 微信搜索：牧小农
 * @create: 2021-10-23 10:45
 * @Version 1.0
 **/
public class TestLock {


    ZooKeeper zk ;

    @Before
    public void conn () throws IOException {
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        zk  =   new ZooKeeper("127.0.0.1:2181",//zk地址
                4000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(Event.KeeperState.SyncConnected==event.getState()){
                    //如果收到了服务端的响应事件，连接成功
                    countDownLatch.countDown();
                }
            }
        });
    }

    @After
    public void close (){
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void lock(){

        //创建十个线程
        for (int i = 0; i < 10; i++) {
            new Thread(){
                @Override
                public void run() {
                    WatchCallBack watchCallBack = new WatchCallBack();
                    watchCallBack.setZk(zk);
                    String threadName = Thread.currentThread().getName();
                    watchCallBack.setThreadName(threadName);
                    //线程进行抢锁操作
                    watchCallBack.tryLock();
                    try {
                        //进行业务逻辑处理
                        System.out.println(threadName+"         开始处理业务逻辑了...");
                        Thread.sleep(200);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //释放锁
                    watchCallBack.unLock();


                }
            }.start();
        }


        while(true){

        }

    }

}
