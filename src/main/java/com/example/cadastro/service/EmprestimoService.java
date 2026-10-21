package com.example.cadastro.service;

import com.example.cadastro.entities.Emprestimo;
import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final ExemplarRepository exemplarRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            ExemplarRepository exemplarRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.exemplarRepository = exemplarRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listar() {
        return emprestimoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarAtivos() {
        return emprestimoRepository.findBySituacao(SituacaoEmprestimo.ATIVO);
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarAtrasados() {
        return emprestimoRepository.findAtrasados(
                SituacaoEmprestimo.ATIVO,
                LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarPorUsuario(Long usuarioId) {
        return emprestimoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> emprestimosAtrasadosPorUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        return emprestimoRepository.findEmprestimosAtrasadosPorUsuario(
                usuarioId,
                SituacaoEmprestimo.ATIVO,
                LocalDate.now()
        );
    }

    @Transactional
    public Emprestimo emprestar(Long exemplarId, Long usuarioId) {
        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElseThrow(() -> new EntityNotFoundException("Exemplar não encontrado"));

        if(!exemplar.isAtivo() || !exemplar.getLivro().isAtivo()) {
            throw new IllegalStateException("O exemplar ou o título está inativo");
        }

        boolean emprestimoAtivoExiste = emprestimoRepository.existsByExemplarIdAndSituacao(exemplarId, SituacaoEmprestimo.ATIVO);
        if (emprestimoAtivoExiste) {
            throw new IllegalStateException("Este exemplar já está emprestado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Não é possível emprestar para um usuário inativo");
        }

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(exemplar, usuario, LocalDate.now());

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo renovar(Long emprestimoId) {
        Emprestimo emprestimoAtual = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        if (!emprestimoAtual.getUsuario().isAtivo()) {
            throw new IllegalStateException("Não é possível renovar para um usuário inativo");
        }

        LocalDate dataRenovacao = LocalDate.now();

        emprestimoAtual.registrarRenovacao(dataRenovacao);
        emprestimoRepository.save(emprestimoAtual);

        Emprestimo novoEmprestimo = new Emprestimo();
        novoEmprestimo.iniciar(
                emprestimoAtual.getExemplar(),
                emprestimoAtual.getUsuario(),
                dataRenovacao
        );

        return emprestimoRepository.save(novoEmprestimo);
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        emprestimo.registrarDevolucao(LocalDate.now());

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularMultaTotalPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }
        List<Emprestimo> emprestimos = listarPorUsuario(usuarioId);
        BigDecimal multas = BigDecimal.ZERO;

        for (Emprestimo emprestimo : emprestimos) {
            multas = multas.add(emprestimo.getMulta());
        }

        return multas;
    }
}