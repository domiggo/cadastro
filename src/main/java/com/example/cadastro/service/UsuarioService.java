package com.example.cadastro.service;

import com.example.cadastro.entities.Usuario;
import com.example.cadastro.repository.EmprestimoRepository;
import com.example.cadastro.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmprestimoRepository emprestimoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRepository = emprestimoRepository;
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
        if (emprestimoRepository.existsByUsuarioIdAndDataDevolucaoIsNull(id)) {
            throw new IllegalArgumentException("Devolva os livros alugados antes de excluir o usuário");
        }
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuarioRepository.delete(usuario);
                    return true;
                })
                .orElse(false);
    }

}