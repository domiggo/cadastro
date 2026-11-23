package com.example.cadastro.dto;

import com.example.cadastro.entities.Emprestimo;
import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaEmprestimosResposta(
        List<EmprestimoResposta> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static PaginaEmprestimosResposta de(Page<Emprestimo> pagina) {
        return new PaginaEmprestimosResposta(
                pagina.map(EmprestimoResposta::de).getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }
}
