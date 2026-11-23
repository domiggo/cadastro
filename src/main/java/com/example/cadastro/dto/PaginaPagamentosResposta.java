package com.example.cadastro.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaginaPagamentosResposta(
        List<PagamentoMultaResumo> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static PaginaPagamentosResposta de(Page<PagamentoMultaResumo> pagina) {
        return new PaginaPagamentosResposta(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );
    }
}
