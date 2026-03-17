package com.aicodereview.webhook.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

    @GetMapping("/health")
    public String health() {
        return "Webhook Service Running";
    }

}