package com.krachBank.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankAccountController {

    @GetMapping("/ping")
    public String test() {
        System.out.println("ping");
        return "pong!";
    }



}
