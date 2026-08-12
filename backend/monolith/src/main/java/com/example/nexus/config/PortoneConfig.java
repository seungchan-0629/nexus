//package com.example.nexus.config;
//
//import io.portone.sdk.server.payment.PaymentClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class PortoneConfig {
//
//    private String apiSecret;
//
//    @Bean
//    public PaymentClient iamportClient() {
//
//        return new PaymentClient(apiSecret, "https://api.portone.io", null);
//    }
//
//}