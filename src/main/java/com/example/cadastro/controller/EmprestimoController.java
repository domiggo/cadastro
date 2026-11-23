package com.example.cadastro.controller;

import com.example.cadastro.dto.EmprestimoResposta;
import com.example.cadastro.dto.PaginaEmprestimosResposta;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.service.EmprestimoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public List<EmprestimoResposta> listar() {
        return emprestimoService.listar().stream()
                .map(EmprestimoResposta::de)
                .toList();
    }

    @GetMapping("/historico")
    public PaginaEmprestimosResposta buscarHistorico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) SituacaoEmprestimo situacao
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
            );
        }

        return PaginaEmprestimosResposta.de(emprestimoService.buscarHistorico(
                usuarioId,
                situacao,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        ));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<EmprestimoResposta> listarPorUsuario(@PathVariable Long usuarioId) {
        return emprestimoService.listarPorUsuario(usuarioId).stream()
                .map(EmprestimoResposta::de)
                .toList();
    }

    @GetMapping("/usuario/{usuarioId}/pagina")
    public PaginaEmprestimosResposta listarPorUsuarioPaginados(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
            );
        }

        return PaginaEmprestimosResposta.de(
                emprestimoService.buscarPorUsuario(
                        usuarioId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
                )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResposta emprestar(
            @RequestParam Long exemplarId,
            @RequestParam Long usuarioId
    ) {
        return EmprestimoResposta.de(emprestimoService.emprestar(exemplarId, usuarioId));
    }

    @PostMapping("/{emprestimoId}/devolucao")
    public EmprestimoResposta devolver(@PathVariable Long emprestimoId) {
        return EmprestimoResposta.de(emprestimoService.devolver(emprestimoId));
    }

    @GetMapping("/ativos")
    public List<EmprestimoResposta> listarAtivos() {
        return emprestimoService.listarAtivos().stream()
                .map(EmprestimoResposta::de)
                .toList();
    }

    @GetMapping("/ativos/pagina")
    public PaginaEmprestimosResposta listarAtivosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
            );
        }

        return PaginaEmprestimosResposta.de(emprestimoService.buscarAtivos(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "dataEmprestimo")
                                .and(Sort.by(Sort.Direction.DESC, "id"))
                )
        ));
    }

    @PostMapping("/{emprestimoId}/renovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public EmprestimoResposta renovar(@PathVariable Long emprestimoId) {
        return EmprestimoResposta.de(emprestimoService.renovar(emprestimoId));
    }

    @GetMapping("/atrasados")
    public List<EmprestimoResposta> listarAtrasados() {
        return emprestimoService.listarAtrasados().stream()
                .map(EmprestimoResposta::de)
                .toList();
    }

    @GetMapping("/atrasados/pagina")
    public PaginaEmprestimosResposta listarAtrasadosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
            );
        }

        return PaginaEmprestimosResposta.de(emprestimoService.buscarAtrasados(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.ASC, "dataPrevistaDevolucao")
                                .and(Sort.by(Sort.Direction.ASC, "id"))
                )
        ));
    }

    @GetMapping("/usuario/{usuarioId}/atrasados")
    public List<EmprestimoResposta> listarEmprestimosAtrasadosPorUsuario(
            @PathVariable Long usuarioId
    ) {
        return emprestimoService.emprestimosAtrasadosPorUsuario(usuarioId).stream()
                .map(EmprestimoResposta::de)
                .toList();
    }

    @GetMapping("/usuario/{usuarioId}/atrasados/pagina")
    public PaginaEmprestimosResposta listarEmprestimosAtrasadosPorUsuarioPaginados(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "A página deve ser não negativa e o tamanho deve estar entre 1 e 100"
            );
        }

        return PaginaEmprestimosResposta.de(
                emprestimoService.buscarEmprestimosAtrasadosPorUsuario(
                        usuarioId,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.ASC, "dataPrevistaDevolucao")
                                        .and(Sort.by(Sort.Direction.ASC, "id"))
                        )
                )
        );
    }

    @GetMapping("/usuario/{usuarioId}/multa-total")
    public BigDecimal listarMultaTotalPorUsuario(@PathVariable Long usuarioId) {
        return emprestimoService.calcularMultaTotalPorUsuario(usuarioId);
    }
}