package com.example.cadastro.controller;

import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.service.LivrosService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivrosController {
    private final LivrosService livrosService;

    public LivrosController(LivrosService livrosService) {
        this.livrosService = livrosService;
    }

    @GetMapping
    public List<LivrosModel> listar() {
        return livrosService.listar();
    }

    @GetMapping("/disponiveis")
    public List<LivrosModel> listarDisponiveis() {
        return livrosService.listarDisponiveis();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LivrosModel cadastrar(@RequestBody LivrosModel livro) {
        return livrosService.cadastrar(livro);
    }

    @DeleteMapping("/{livroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable long livroId) {
        livrosService.excluir(livroId);
    }
}