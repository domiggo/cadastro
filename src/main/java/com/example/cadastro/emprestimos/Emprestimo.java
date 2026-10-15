package com.example.cadastro.emprestimos;


import com.example.cadastro.entities.LivrosModel;
import com.example.cadastro.entities.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tb_emprestimos")
@Getter
@Setter
@NoArgsConstructor
public class Emprestimo {

    private static final int PRAZO_EM_DIAS = 7;
    private static final BigDecimal MULTA_POR_DIA = new BigDecimal("2.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id", nullable = false)
    private LivrosModel livro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @Column(nullable = false)
    private LocalDate dataPrevistaDevolucao;

    //NULA ENQUANTO O LIVRO NÃO FOR DEVOLVIDO
    private LocalDate dataDevolucao;

    public void iniciar(LivrosModel livro, Usuario usuario, LocalDate dataEmprestimo) {
        this.livro = livro;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataEmprestimo.plusDays(PRAZO_EM_DIAS);
        this.dataDevolucao = null;
    }

    public void registrarDevolucao(LocalDate dataDevolucao) {
        if (!isAtivo()) {
            throw new IllegalStateException("Este empréstimo jafoi devolvido");
        }

        if (dataDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior ao empréstimo");
        }
        this.dataDevolucao = dataDevolucao;
    }

    public boolean isAtivo() {
        return dataDevolucao == null;
    }

    public long calcularDiasDeAtraso(LocalDate dataReferencia) {
        LocalDate dataFinal = dataDevolucao == null ? dataReferencia : dataDevolucao;

        if (!dataFinal.isAfter(dataPrevistaDevolucao)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(dataPrevistaDevolucao, dataFinal);
    }

    public BigDecimal calcularMulta(LocalDate dataReferencia) {
        long diasAtrasado = calcularDiasDeAtraso(dataReferencia);

        return MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasAtrasado));
    }

    //PROPRIEDADES CALCULADAS QUE TAMBÉM PODE APARECER NA RESPOSTA
    public long getDiasDeAtraso() {
        return calcularDiasDeAtraso(LocalDate.now());
    }

    public BigDecimal getMulta() {
        return calcularMulta(LocalDate.now());
    }
}
