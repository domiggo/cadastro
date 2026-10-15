package com.example.cadastro.controller;

import com.example.cadastro.emprestimos.Emprestimo;
import com.example.cadastro.service.EmprestimoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @GetMapping
    public List<Emprestimo> listar() {
        return emprestimoService.listar();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Emprestimo> listarPorUsuario(@PathVariable Long usuarioId) {
        return emprestimoService.listarPorUsuario(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Emprestimo emprestar(@RequestParam Long livroId, @RequestParam Long usuarioId) {
        return emprestimoService.emprestar(livroId, usuarioId);
    }

    @PostMapping("/{emprestimoId}/devolucao")
    public Emprestimo devolver(@PathVariable Long emprestimoId) {
        return emprestimoService.devolver(emprestimoId);
    }
}
