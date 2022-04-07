package com.example.practicesse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PracticeSseApplication {

    public static void main(String[] args) {
        System.setProperty("reactor.netty.ioWorkerCount", "2");
        System.setProperty("reactor.netty.ioSelectCount", "2");
        System.setProperty("reactor.netty.pool.maxConnections", "2");

        SpringApplication.run(PracticeSseApplication.class, args);
    }

}
