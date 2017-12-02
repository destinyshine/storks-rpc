package test;

import java.awt.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Pandora Boot应用的入口类
 * <p>
 * 其中@DiamondPropertySource是导入来自Diamond Server的配置，详情见
 * http://gitlab.alibaba-inc.com/middleware-container/pandora-boot/wikis/spring-boot-diamond
 *
 * @author chengxu
 */
@SpringBootApplication()
public class Run {

    public static void main(String[] args) {
        SpringApplication.run(Run.class, args);
    }

    @Bean
    public Point point() {
        return new Point();
    }

    @Bean
    public Object xx() {
        return new Xx();
    }

    public static class Xx {

        Point point;

        @Autowired
        public void setPoint(Point point) {
            this.point = point;
        }
    }
}
