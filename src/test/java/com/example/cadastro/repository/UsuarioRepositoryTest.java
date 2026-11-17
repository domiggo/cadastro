package com.example.cadastro.repository;

import com.example.cadastro.entities.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @Transactional
    void deveImpedirCpfDuplicadoNoBanco() {
        String cpf = Long.toString(
                ThreadLocalRandom.current().nextLong(10_000_000_000L, 100_000_000_000L)
        );

        usuarioRepository.saveAndFlush(criarUsuario("primeiro", cpf));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioRepository.saveAndFlush(criarUsuario("segundo", cpf))
        );
    }

    private Usuario criarUsuario(String identificador, String cpf) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário " + identificador);
        usuario.setEmail(identificador + "@example.com");
        usuario.setCpf(cpf);
        return usuario;
    }
}
