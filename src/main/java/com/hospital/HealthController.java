package com.hospital.appiontment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "Hospital Appointment App is running";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
