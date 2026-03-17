package com.aicodereview.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @GetMapping("/health")
    public String health() {
        return "Notification Service Running";
    }
}