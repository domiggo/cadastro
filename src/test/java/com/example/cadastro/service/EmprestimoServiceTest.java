package com.example.cadastro.service;

import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.ExemplarRepository;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


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
}
