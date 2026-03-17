package com.aicodereview.storage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageController {

    @GetMapping("/health")
    public String health() {
        return "Review Storage Service Running";
    }
}