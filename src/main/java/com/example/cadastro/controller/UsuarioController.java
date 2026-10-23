package com.example.cadastro.controller;

import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.service.PagamentoMultaService;
import com.example.cadastro.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PagamentoMultaService pagamentoMultaService;


    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/inativos")
    public List<Usuario> listarInativos() {
        return usuarioService.listarInativos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarporId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        Usuario usuarioCriado = usuarioService.criar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @Valid @RequestBody Usuario dadosRecebidos) {
        return usuarioService.atualizar(id, dadosRecebidos)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        if (!usuarioService.desativar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativacao")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        if (!usuarioService.ativar(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{usuarioId}/multas/saldo-pendente")
    public BigDecimal consultarSaldoMultasPendentesPorUsuario(@PathVariable Long usuarioId) {
        return pagamentoMultaService.calcularSaldoMultasPendentesPorUsuario(usuarioId);
    }

    @GetMapping("/{usuarioId}/pagamentos")
    public List<PagamentoMulta> listarPagamentosPorUsuario(@PathVariable Long usuarioId) {
        return pagamentoMultaService.listarPagamentosPorUsuario(usuarioId);
    }
}