package com.example.cadastro.service;

import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByCpf(usuario.getCpf())) {
            throw new IllegalStateException("Já existe um usuário cadastrado com esse CPF");
        }
        usuario.setId(null);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Optional<Usuario> atualizar(Long id, Usuario dadosRecebidos) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    if (usuarioRepository.existsByCpfAndIdNot(dadosRecebidos.getCpf(), id)) {
                        throw new IllegalStateException(
                                "Já existe outro usuário cadastrado com esse CPF"
                        );
                    }

                    usuario.setNome(dadosRecebidos.getNome());
                    usuario.setEmail(dadosRecebidos.getEmail());
                    usuario.setCpf(dadosRecebidos.getCpf());
                    return usuarioRepository.save(usuario);
                });
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public boolean desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

        if (usuario == null) {
            return false;
        }

        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        return true;
    }
}