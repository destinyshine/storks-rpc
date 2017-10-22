package io.destinyshine.storks.sample.service.impl;

import io.destinyshine.storks.sample.service.api.HelloService;

/**
 * @author destinyliu
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "hello, " + name;
    }
}
