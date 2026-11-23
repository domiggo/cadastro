package com.example.cadastro.controller;

import com.example.cadastro.dto.ResumoBiblioteca;
import com.example.cadastro.service.ResumoBibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResumoBibliotecaController {

    private final ResumoBibliotecaService resumoBibliotecaService;

    public ResumoBibliotecaController(ResumoBibliotecaService resumoBibliotecaService) {
        this.resumoBibliotecaService = resumoBibliotecaService;
    }

    @GetMapping("/biblioteca/resumo")
    public ResumoBiblioteca consultar() {
        return resumoBibliotecaService.consultar();
    }
}
