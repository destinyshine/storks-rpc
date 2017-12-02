package io.destinyshine.storks.test.service.impl;

import io.destinyshine.storks.spring.boot.StorksProvider;
import io.destinyshine.storks.test.service.api.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author destinyliu
 */
@Slf4j
@StorksProvider
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        logger.info("do biz, name={}", name);
        return "hello, " + name;
    }
}
