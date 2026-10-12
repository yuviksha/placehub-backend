package com.placementhub.placementhub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to PlacementHub Backend! Server is running successfully.";
    }

    @GetMapping("/api/health")
    public String health() {
        return "PlacementHub API is healthy!";
    }
}