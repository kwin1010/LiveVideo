package com.example.livephoto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LivePhotoApplication {
    public static void main(String[] args) {
        SpringApplication.run(LivePhotoApplication.class, args);
    }
}
