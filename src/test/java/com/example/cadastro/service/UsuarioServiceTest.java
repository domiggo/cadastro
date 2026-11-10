package com.example.cadastro.service;

import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void deveAtualizarQuandoCpfForDoProprioUsuario() {
        Long id = 1L;
        String cpf = "12345678900";

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(id);
        usuarioExistente.setNome("Ana Silva");
        usuarioExistente.setEmail("ana@example.com");
        usuarioExistente.setCpf(cpf);

        Usuario dadosRecebidos = new Usuario();
        dadosRecebidos.setNome("Ana Souza");
        dadosRecebidos.setEmail("ana.souza@example.com");
        dadosRecebidos.setCpf(cpf);

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.of(usuarioExistente));
        when(usuarioRepository.existsByCpfAndIdNot(cpf, id))
                .thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Optional<Usuario> resultado = usuarioService.atualizar(id, dadosRecebidos);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Souza", resultado.get().getNome());
        assertEquals("ana.souza@example.com", resultado.get().getEmail());
        assertEquals(cpf, resultado.get().getCpf());

        verify(usuarioRepository, times(1)).save(usuarioExistente);
    }

    @Test
    void deveRetornarVazioQuandoUsuarioNaoExistirAoAtualizar() {
        Long id = 1L;

        Usuario dadosRecebidos = new Usuario();
        dadosRecebidos.setNome("dominggo");
        dadosRecebidos.setEmail("dominggo@mail.com");
        dadosRecebidos.setCpf("12345678910");

        when(usuarioRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.atualizar(id, dadosRecebidos);

        assertTrue(resultado.isEmpty());

        verify(usuarioRepository, never()).existsByCpfAndIdNot(anyString(), anyLong());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveFalharAoCriarQuandoCpfJaEstiverCadastrado() {
        String cpf = "12345678910";

        Usuario usuario = new Usuario();
        usuario.setNome("dominggo");
        usuario.setEmail("dominggo@mail.com");
        usuario.setCpf(cpf);

        when(usuarioRepository.existsByCpf("12345678910"))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                usuarioService.criar(usuario));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void deveCriarUsuarioComIdNuloEAtivo() {
        String cpf = "12345678910";
        String nome = "dominggo";
        String email = "dominggo@mail.com";

        Usuario usuario = new Usuario();
        usuario.setId(99L);
        usuario.setAtivo(false);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setCpf(cpf);

        when(usuarioRepository.existsByCpf("12345678910"))
                .thenReturn(false);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario resultado = usuarioService.criar(usuario);

        assertNull(resultado.getId());
        assertTrue(resultado.isAtivo());
        assertEquals(nome, resultado.getNome());
        assertEquals(email, resultado.getEmail());
        assertEquals(cpf, resultado.getCpf());

        verify(usuarioRepository, times(1)).save(usuario);
    }
}