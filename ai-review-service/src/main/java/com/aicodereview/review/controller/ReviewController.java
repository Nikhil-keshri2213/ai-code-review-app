package com.aicodereview.review.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    @GetMapping("/health")
    public String health() {
        return "Ai Code Review Service Running";
    }
}