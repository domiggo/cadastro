package com.example.cadastro.service;

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
public class LivrosService {

    private final LivrosRepository livrosRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ExemplarRepository exemplarRepository;

    public LivrosService(
            LivrosRepository livrosRepository,
            EmprestimoRepository emprestimoRepository,
            ExemplarRepository exemplarRepository
    ) {
        this.livrosRepository = livrosRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.exemplarRepository = exemplarRepository;
    }

    @Transactional(readOnly = true)
    public List<LivrosModel> listar() {
        return livrosRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<LivrosModel> listarDisponiveis() {
        return livrosRepository.findDisponiveis(SituacaoEmprestimo.ATIVO);
    }

    @Transactional(readOnly = true)
    public List<LivrosModel> listarInativos() {
        return livrosRepository.findByAtivoFalse();
    }


    @Transactional
    public LivrosModel cadastrar(LivrosModel livro) {
        // O ID é criado pelo banco, não deve vir definido pelo cliente.
        livro.setId(null);
        livro.setAtivo(true);
        return livrosRepository.save(livro);
    }

    @Transactional
    public boolean desativar(Long livroId) {
        LivrosModel livro = livrosRepository.findById(livroId)
                .orElse(null);

        if (livro == null) {
            return false;
        }

        livro.setAtivo(false);
        livrosRepository.save(livro);
        return true;
    }

    @Transactional
    public boolean ativar(Long livroId) {
        LivrosModel livro = livrosRepository.findById(livroId)
                .orElse(null);

        if (livro == null) {
            return false;
        }

        livro.setAtivo(true);
        livrosRepository.save(livro);
        return true;
    }

    @Transactional
    public void excluir(Long livroId) {
        LivrosModel livro = livrosRepository.findById(livroId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Livro não encontrado"));

        // Impede apagar um livro referenciado quando há emprestimos ativos vinculados ao livro
        if (emprestimoRepository.existeEmprestimoAtivoPorLivroESituacao(livroId, SituacaoEmprestimo.ATIVO)) {
            throw new IllegalStateException(
                    "Não é possível excluir um livro com exemplares emprestados"
            );
        }

        emprestimoRepository.excluirHistoricoDoLivro(livroId);
        exemplarRepository.excluirPorLivroId(livroId);
        livrosRepository.delete(livro);
    }
}