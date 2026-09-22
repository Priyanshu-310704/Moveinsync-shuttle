package com.moveinsync.shuttle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ShuttleBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShuttleBookingApplication.class, args);
    }
}
