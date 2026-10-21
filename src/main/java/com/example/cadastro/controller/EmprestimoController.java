package com.example.cadastro.controller;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.service.EmprestimoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public Emprestimo emprestar(@RequestParam Long exemplarId, @RequestParam Long usuarioId) {
        return emprestimoService.emprestar(exemplarId, usuarioId);
    }

    @PostMapping("/{emprestimoId}/devolucao")
    public Emprestimo devolver(@PathVariable Long emprestimoId) {
        return emprestimoService.devolver(emprestimoId);
    }

    @GetMapping("/ativos")
    public List<Emprestimo> listarAtivos() {
        return emprestimoService.listarAtivos();
    }

    @PostMapping("/{emprestimoId}/renovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public Emprestimo renovar(@PathVariable Long emprestimoId) {
        return emprestimoService.renovar(emprestimoId);
    }

    @GetMapping("/atrasados")
    public List<Emprestimo> listarAtrasados() {
        return emprestimoService.listarAtrasados();
    }

    @GetMapping("/usuario/{usuarioId}/atrasados")
    public List<Emprestimo> listarEmprestimosAtrasadosPorUsuario(@PathVariable Long usuarioId) {
        return emprestimoService.emprestimosAtrasadosPorUsuario(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/multa-total")
    public BigDecimal listarMultaTotalPorUsuario(@PathVariable Long usuarioId) {
        return emprestimoService.calcularMultaTotalPorUsuario(usuarioId);
    }
}