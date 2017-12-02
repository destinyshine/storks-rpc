package io.destinyshine.storks.test.spring.boot.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.destinyshine.storks.test.service", "io.destinyshine.storks.test.spring.boot.server"})
public class ServerApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(ServerApplication.class, args);
        Thread.currentThread().join();
    }

}
