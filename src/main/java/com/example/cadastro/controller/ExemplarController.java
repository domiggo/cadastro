package com.example.cadastro.controller;

import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.service.ExemplarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class ExemplarController {

    private final ExemplarService exemplarService;

    public ExemplarController(ExemplarService exemplarService) {
        this.exemplarService = exemplarService;
    }

    @PostMapping("/{livroId}/exemplares")
    @ResponseStatus(HttpStatus.CREATED)
    public Exemplar cadastrar(@PathVariable Long livroId, @RequestParam String codigoPatrimonio) {
        return exemplarService.cadastrar(livroId, codigoPatrimonio);
    }

    @GetMapping("/{livroId}/exemplares/ativos")
    public List<Exemplar> listarExemplaresAtivos(@PathVariable Long livroId) {
        return exemplarService.listarAtivosPorLivro(livroId);
    }

    @GetMapping("/{livroId}/exemplares/disponiveis")
    public List<Exemplar> listarExemplaresDisponiveis(@PathVariable Long livroId) {
        return exemplarService.listarDisponiveisPorLivro(livroId);
    }

    @PatchMapping("/{livroId}/exemplares/{exemplarId}/desativacao")
    public ResponseEntity<Void> desativar(
            @PathVariable Long livroId,
            @PathVariable Long exemplarId
    ) {
        if (!exemplarService.desativar(livroId, exemplarId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
