package com.example.PlataformaDarcy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/aluno/home")
    public String alunoHome() {
        return "aluno-home";
    }
}