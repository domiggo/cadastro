package com.example.cadastro.service;

import com.example.cadastro.dto.ResumoBiblioteca;
import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ResumoBibliotecaService {

    private final UsuarioRepository usuarioRepository;
    private final LivrosRepository livrosRepository;
    private final EmprestimoRepository emprestimoRepository;

    public ResumoBibliotecaService(
            UsuarioRepository usuarioRepository,
            LivrosRepository livrosRepository,
            EmprestimoRepository emprestimoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.livrosRepository = livrosRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoBiblioteca consultar() {
        return new ResumoBiblioteca(
                usuarioRepository.countByAtivoTrue(),
                livrosRepository.countByAtivoTrue(),
                emprestimoRepository.countBySituacao(SituacaoEmprestimo.ATIVO),
                emprestimoRepository.countBySituacaoAndDataPrevistaDevolucaoBefore(
                        SituacaoEmprestimo.ATIVO,
                        LocalDate.now()
                )
        );
    }
}
