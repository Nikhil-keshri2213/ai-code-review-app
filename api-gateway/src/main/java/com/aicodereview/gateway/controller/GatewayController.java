package com.aicodereview.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {

    @GetMapping("/health")
    public String health() {
        return "Api-Gateway Service Running";
    }
}