package com.example.cadastro.repository;

import com.example.cadastro.entities.PagamentoMulta;
import com.example.cadastro.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);

    List<Usuario> findByAtivoTrue();
    List<Usuario> findByAtivoFalse();
}
