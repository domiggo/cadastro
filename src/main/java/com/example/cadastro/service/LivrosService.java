package com.example.cadastro.service;

import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.LivrosRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivrosService {

    private final LivrosRepository livrosRepository;
    private final EmprestimoRepository emprestimoRepository;

    public LivrosService(
            LivrosRepository livrosRepository,
            EmprestimoRepository emprestimoRepository
    ) {
        this.livrosRepository = livrosRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<LivrosModel> listar() {
        return livrosRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LivrosModel> listarDisponiveis() {
        return livrosRepository.findDisponiveis();
    }

    @Transactional
    public LivrosModel cadastrar(LivrosModel livro) {
        // O ID é criado pelo banco, não deve vir definido pelo cliente.
        livro.setId(null);
        return livrosRepository.save(livro);
    }

    @Transactional
    public void excluir(Long livroId) {
        LivrosModel livro = livrosRepository.findById(livroId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Livro não encontrado"));

        // Impede apagar um livro referenciado pelo histórico de empréstimos.
        if (emprestimoRepository.existsByLivroId(livroId)) {
            throw new IllegalStateException(
                    "Não é possível excluir um livro que possui "
                            + "empréstimos no histórico"
            );
        }

        livrosRepository.delete(livro);
    }
}