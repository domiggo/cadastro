package com.example.cadastro.service;

import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.EmprestimoDatasMulta;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;
    @Mock
    private ExemplarRepository exemplarRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmprestimoService emprestimoService;

    @Test
    void deveFalharAoListarPorUsuario() {
        Long usuarioId = 1L;

        when(usuarioRepository.existsById(usuarioId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                emprestimoService.listarPorUsuario(usuarioId)
        );

        verify(emprestimoRepository, never())
                .findByUsuarioId(usuarioId);
    }

    @Test
    void deveFalharAoCalcularMultaTotalDeUsuarioInexistente() {
        Long usuarioId = 1L;

        when(usuarioRepository.existsById(usuarioId))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () ->
                emprestimoService.calcularMultaTotalPorUsuario(usuarioId));

        verify(emprestimoRepository, never()).buscarDatasMultaPorUsuario(usuarioId);
    }

    @Test
    void deveCalcularMultaTotalComConsultaSomenteDeDatas() {
        Long usuarioId = 1L;
        LocalDate hoje = LocalDate.now();
        EmprestimoDatasMulta vencido = mock(EmprestimoDatasMulta.class);
        when(vencido.getDataPrevistaDevolucao()).thenReturn(hoje.minusDays(10));
        when(vencido.getDataDevolucao()).thenReturn(null);
        when(vencido.getDataEncerramento()).thenReturn(null);

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(emprestimoRepository.buscarDatasMultaPorUsuario(usuarioId))
                .thenReturn(List.of(vencido));

        assertEquals(
                new BigDecimal("20.00"),
                emprestimoService.calcularMultaTotalPorUsuario(usuarioId)
        );
        verify(emprestimoRepository).buscarDatasMultaPorUsuario(usuarioId);
        verify(emprestimoRepository, never()).findByUsuarioId(usuarioId);
    }
}
