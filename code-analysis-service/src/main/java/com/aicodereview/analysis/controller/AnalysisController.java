package com.aicodereview.analysis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalysisController {

    @GetMapping("/health")
    public String health() {
        return "Code Analysis Service Running";
    }
}