package com.ryml.controller;

import com.ryml.annotation.MyAutoWriter;
import com.ryml.annotation.MyController;
import com.ryml.annotation.MyRequestMapping;
import com.ryml.service.DemoService;

/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/2/28
 */
@MyController
@MyRequestMapping("/demo")
public class DemoController {

    @MyAutoWriter("myDemoServiceImpl")
    private DemoService demoService;

    @MyRequestMapping("/test")
    public String demo(String name){
        return demoService.demo(name);
    }

}
