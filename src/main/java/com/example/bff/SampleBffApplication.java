package com.example.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.xray.AWSXRay;

@SpringBootApplication
public class SampleBffApplication {

    public static void main(String[] args) {
        AWSXRay.beginSegment("sample-bff");
        // Spring Boot AP起動
        SpringApplication.run(SampleBffApplication.class, args);
        
        // FlightRecorderApplicationStartupを使った、JFRのアプリケーションのスタートアップの追跡する場合のAP起動例
        // https://spring.pleiades.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.startup-tracking
        /*
        SpringApplication app = new SpringApplication(SampleBffApplication.class);
        app.setApplicationStartup(new FlightRecorderApplicationStartup());
        app.run(args);
        */
    }

}
