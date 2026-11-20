package com.example.pickup;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication @EnableFeignClients public class PickupSchedulingApplication {public static void main(String[] args){SpringApplication.run(PickupSchedulingApplication.class,args);}}
