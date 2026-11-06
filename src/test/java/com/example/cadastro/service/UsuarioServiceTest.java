package com.example.cadastro.service;

import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deveFalharAoAtualizarQuandoCpfPertencerAOutroUsuario() {
        Long id = 1L;
        String cpfDeOutroUsuario = "98765432100";

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(id);
        usuarioExistente.setNome("Dominggo");
        usuarioExistente.setEmail("dominggo@mail.com");
        usuarioExistente.setCpf("12345678900");

        Usuario dadosRecebidos = new Usuario();
        dadosRecebidos.setNome("dominggo");
        dadosRecebidos.setEmail("dominggo@mail.com");
        dadosRecebidos.setCpf(cpfDeOutroUsuario);

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByCpfAndIdNot(cpfDeOutroUsuario, id))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                usuarioService.atualizar(id, dadosRecebidos)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
