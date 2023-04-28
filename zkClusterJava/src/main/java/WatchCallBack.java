import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * @program: mxnzookeeper
 * @ClassName WatchCallBack
 * @description:
 * @author: 微信搜索：牧小农
 * @create: 2021-10-23 10:48
 * @Version 1.0
 **/
public class WatchCallBack  implements Watcher, AsyncCallback.StringCallback ,AsyncCallback.Children2Callback ,AsyncCallback.StatCallback {

    ZooKeeper zk ;
    String threadName;
    CountDownLatch cc = new CountDownLatch(1);
    String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    /** @Author 牧小农
     * @Description //TODO 尝试加锁方法
     * @Date 16:14 2021/10/24
     * @Param
     * @return
     **/
    public void tryLock(){
        try {

            System.out.println(threadName + " 开始创建。。。。");
            //创建一个顺序临时节点
            zk.create("/lock",threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,"abc");
            //阻塞当前，监听前一个节点是否释放锁
            cc.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** @Author 牧小农
     * @Description //TODO 解锁方法
     * @Date 16:14 2021/10/24
     * @Param
     * @return
     **/
    public void unLock(){
        try {
            //释放锁，删除临时节点
            zk.delete(pathName,-1);
            //结束工作
            System.out.println(threadName + "         结束工作了....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void process(WatchedEvent event) {

        //如果第一个节点释放了锁，那么第二个就会收到回调
        //告诉它前一个节点释放了，你可以开始尝试获取锁
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                //当前节点重新获取锁
                zk.getChildren("/",false,this ,"sdf");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if(name != null ){
            System.out.println(threadName  +" 线程创建了一个节点为 : " +  name );
            pathName =  name ;
            //监听前一个节点
            zk.getChildren("/",false,this ,"sdf");
        }

    }

    //getChildren  call back
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {

        //节点按照编号，升序排列
        Collections.sort(children);
        //对节点进行截取例如  /lock0000000022 截取后就是  lock0000000022
        int i = children.indexOf(pathName.substring(1));


        //是不是第一个，也就是说是不是最小的
        if(i == 0){
            //是第一个
            System.out.println(threadName +" 现在我是最小的....");
            try {
                zk.setData("/",threadName.getBytes(),-1);
                cc.countDown();

            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            //不是第一个
            //监听前一个节点 看它是不是完成了工作进行释放锁了
            zk.exists("/"+children.get(i-1),this,this,"sdf");
        }

    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //判断是否失败exists
    }
}

