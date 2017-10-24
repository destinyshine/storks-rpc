package io.destinyshine.storks.sample.service.impl;

import io.destinyshine.storks.sample.service.api.HelloService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author destinyliu
 */
@Slf4j
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        logger.info("do biz, name={}", name);
        return "hello, " + name;
    }
}
