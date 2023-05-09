package com.lsgf.zookeeperdemo.controller;

import com.lsgf.zookeeperdemo.config.ZookeeperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
public class ZkController {

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    @GetMapping("get/{key}")
    public String getProperties(@PathVariable String key) {
        String value= zookeeperConfig.getProperties(key);
        return value;
    }

    @GetMapping("set/{key}/{value}")
    public String setProperties(@PathVariable String key, @PathVariable String value) throws Exception {
        zookeeperConfig.setProperties(key, value);
        return "配置成功";
    }

    @GetMapping("tget/{key}")
    public String testMutilGetProperties(@PathVariable String key) {
        System.out.println("" + new Date() + " : start : ThreadId " + Thread.currentThread().getId() + " : param : " +key);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String value= zookeeperConfig.getProperties(key);
        System.out.println("" + new Date() + " : end : ThreadId " + Thread.currentThread().getId() + " : param : " +value);
        return value;
    }


    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String test(){
        return "ok";
    }
}


