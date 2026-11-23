package com.example.cadastro.service;

import com.example.cadastro.entities.SituacaoEmprestimo;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.LivrosRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoBibliotecaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LivrosRepository livrosRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private ResumoBibliotecaService resumoBibliotecaService;

    @Test
    void deveConsultarContagensSemCarregarListas() {
        when(usuarioRepository.countByAtivoTrue()).thenReturn(12L);
        when(livrosRepository.countByAtivoTrue()).thenReturn(8L);
        when(emprestimoRepository.countBySituacao(SituacaoEmprestimo.ATIVO))
                .thenReturn(5L);
        when(emprestimoRepository.countBySituacaoAndDataPrevistaDevolucaoBefore(
                SituacaoEmprestimo.ATIVO,
                LocalDate.now()
        )).thenReturn(2L);

        var resumo = resumoBibliotecaService.consultar();

        assertEquals(12L, resumo.usuariosAtivos());
        assertEquals(8L, resumo.livrosAtivos());
        assertEquals(5L, resumo.emprestimosAtivos());
        assertEquals(2L, resumo.emprestimosAtrasados());
        verify(usuarioRepository).countByAtivoTrue();
        verify(livrosRepository).countByAtivoTrue();
        verify(emprestimoRepository).countBySituacao(SituacaoEmprestimo.ATIVO);
        verify(emprestimoRepository).countBySituacaoAndDataPrevistaDevolucaoBefore(
                SituacaoEmprestimo.ATIVO,
                LocalDate.now()
        );
    }
}
