package com.ryml.service.impl;

import com.ryml.annotation.MyService;
import com.ryml.service.DemoService;

/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/2/28
 */
@MyService
public class MyDemoServiceImpl implements DemoService{

    @Override
    public String demo(String name) {
        return "My name is "+name;
    }
}
