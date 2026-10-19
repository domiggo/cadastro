package com.example.cadastro.service;

import com.example.cadastro.entities.Exemplar;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.LivrosRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExemplarService {

    private final ExemplarRepository exemplarRepository;
    private final LivrosRepository livrosRepository;
    private final EmprestimoRepository emprestimoRepository;

    public ExemplarService(
            ExemplarRepository exemplarRepository,
            LivrosRepository livrosRepository,
            EmprestimoRepository emprestimoRepository)
    {
        this.exemplarRepository = exemplarRepository;
        this.livrosRepository = livrosRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional
    public Exemplar cadastrar(Long livroId, String codigoPatrimonio) {
        if (codigoPatrimonio == null || codigoPatrimonio.isBlank()) {
            throw new IllegalArgumentException("Código de patrimônio é obrigatório");
        }

        String codigoNormalizado = codigoPatrimonio.trim();

        if (exemplarRepository.existsByCodigoPatrimonio(codigoNormalizado)) {
            throw new IllegalStateException("Já existe um exemplar com esse código");
        }

        LivrosModel livro = livrosRepository.findById(livroId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));

        if (!livro.isAtivo()) {
            throw new IllegalStateException("Não é possível cadastrar exemplar para um livro inativo");
        }

        Exemplar exemplar = new Exemplar();
        exemplar.setLivro(livro);
        exemplar.setCodigoPatrimonio(codigoNormalizado);

        return exemplarRepository.save(exemplar);
    }

    @Transactional(readOnly = true)
    public List<Exemplar> listarAtivosPorLivro(Long livroId) {
        if (!livrosRepository.existsById(livroId)) {
            throw new EntityNotFoundException("Livro não encontrado");
        }

        return exemplarRepository.findByLivroIdAndAtivoTrue(livroId);
    }

    @Transactional(readOnly = true)
    public List<Exemplar> listarDisponiveisPorLivro(Long livroId) {
        if (!livrosRepository.existsById(livroId)) {
            throw new EntityNotFoundException("Livro não encontrado");
        }

        return exemplarRepository.findDisponiveisPorLivro(
                livroId,
                SituacaoEmprestimo.ATIVO
        );
    }

    @Transactional
    public boolean desativar(Long livroId, Long exemplarId) {
        Exemplar exemplar = exemplarRepository.findById(exemplarId)
                .orElse(null);
        if (exemplar == null) {
            return false;
        }

        if (!exemplar.getLivro().getId().equals(livroId)) {
            return false;
        }

        if (emprestimoRepository.existsByExemplarIdAndSituacao(
                exemplarId,
                SituacaoEmprestimo.ATIVO
        )) {
            throw new IllegalStateException("Não é possível desativar um exemplar emprestado");
        }

        exemplar.setAtivo(false);
        exemplarRepository.save(exemplar);
        return true;
    }
}
