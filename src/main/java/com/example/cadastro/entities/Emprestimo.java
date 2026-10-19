package com.example.cadastro.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JoinColumn(name = "exemplar_id", nullable = false)
    private Exemplar exemplar;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDate dataPrevistaDevolucao;

    //NULA ENQUANTO O LIVRO NÃO FOR DEVOLVIDO
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataDevolucao;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dataEncerramento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoEmprestimo situacao = SituacaoEmprestimo.ATIVO;

    public void iniciar(Exemplar exemplar, Usuario usuario, LocalDate dataEmprestimo) {
        this.exemplar = exemplar;
        this.usuario = usuario;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataEmprestimo.plusDays(PRAZO_EM_DIAS);
        this.dataDevolucao = null;
        this.situacao = SituacaoEmprestimo.ATIVO;
        this.dataEncerramento = null;
    }

    public void registrarRenovacao(LocalDate dataRenovacao) {
        if (!isAtivo()) {
            throw new IllegalStateException("Somente um empréstimo ativo pode ser renovado");
        }

        if (!dataRenovacao.isEqual(dataPrevistaDevolucao)) {
            throw new IllegalArgumentException("A renovação deve ser feita no dia previsto para a devolução");
        }

        this.dataEncerramento = dataRenovacao;
        this.situacao = SituacaoEmprestimo.RENOVADO;
    }

    public void registrarDevolucao(LocalDate dataDevolucao) {
        if (!isAtivo()) {
            throw new IllegalStateException("Este empréstimo ja foi devolvido");
        }

        if (dataDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior ao empréstimo");
        }
        this.dataDevolucao = dataDevolucao;
        this.situacao = SituacaoEmprestimo.DEVOLVIDO;
    }

    public boolean isAtivo() {
        return situacao == SituacaoEmprestimo.ATIVO;
    }

    public long calcularDiasDeAtraso(LocalDate dataReferencia) {
        LocalDate dataFinal;

        if (dataDevolucao != null) {
            dataFinal = dataDevolucao;
        } else if (dataEncerramento != null) {
            dataFinal = dataEncerramento;
        } else {
            dataFinal = dataReferencia;
        }

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