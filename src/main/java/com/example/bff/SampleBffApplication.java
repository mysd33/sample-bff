package com.example.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.xray.AWSXRay;



@SpringBootApplication
public class SampleBffApplication {	

	public static void main(String[] args) {
		AWSXRay.beginSegment("sample-bff");		
		SpringApplication.run(SampleBffApplication.class, args);
	}

}
