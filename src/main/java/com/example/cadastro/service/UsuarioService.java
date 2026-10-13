package com.example.cadastro.service;

import com.example.cadastro.model.Usuario;
import com.example.cadastro.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

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
        usuario.setId(null);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Optional<Usuario> atualizar(Long id, Usuario dadosRecebidos) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
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
    public boolean excluir(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return true;
                })
                .orElse(false);
    }
}