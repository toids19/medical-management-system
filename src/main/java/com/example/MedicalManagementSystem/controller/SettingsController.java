package com.example.MedicalManagementSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
    
    @GetMapping("/help")
    public String help() {
        return "help";
    }
}

