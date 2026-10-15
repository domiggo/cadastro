package com.example.cadastro.service;

import com.example.cadastro.emprestimos.Emprestimo;
import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivrosRepository livrosRepository;
    private final UsuarioRepository usuarioRepository;

    public EmprestimoService(
            EmprestimoRepository emprestimoRepository,
            LivrosRepository livrosRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.livrosRepository = livrosRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listar() {
        return emprestimoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarAtivos() {
        return emprestimoRepository.findByDataDevolucaoIsNull();
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarPorUsuario(Long usuarioId) {
        return emprestimoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Emprestimo emprestar(Long livroId, Long usuarioId) {
        LivrosModel livro = livrosRepository.findById(livroId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));


        boolean livroJaemprestado = emprestimoRepository.findByLivroIdAndDataDevolucaoIsNull(livroId).isPresent();

        if(livroJaemprestado) {
            throw new IllegalArgumentException("O livro já está emprestado no momento");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.iniciar(livro, usuario, LocalDate.now());

        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        emprestimo.registrarDevolucao(LocalDate.now());

        return emprestimoRepository.save(emprestimo);
    }
}
