package com.aicodereview.fetch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FetchController {

    @GetMapping("/health")
    public String health() {
        return "Code Fetch Service Running";
    }
}