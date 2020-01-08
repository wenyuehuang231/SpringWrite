package com.yue.example.service.impl;

import com.yue.example.service.IDemoService;
import com.yue.mvcframework.v1.annotation.YService;

/**
 * created by Mr.huang on 2020/1/8
 */
@YService
public class DemoService implements IDemoService {
    
    @Override
    public String get(String name) {
        return "My name is " + name;
    }

}
