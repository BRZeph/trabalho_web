package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class CriarAssinaturaRequest {

    @NotNull
    private Long planoId;

    @NotBlank
    private String numeroCartao;

    @NotBlank
    private String validadeMes;

    @NotBlank
    private String validadeAno;

    @NotBlank
    private String nomeNoCartao;

    @NotBlank
    private String cvv;

    @NotBlank
    private String tipoPagamento; // "Crédito" ou "Débito"

    @Positive
    private Integer parcelas;

    public @NotNull Long getPlanoId() {
        return planoId;
    }

    public @NotBlank String getNumeroCartao() {
        return numeroCartao;
    }

    public @NotBlank String getValidadeMes() {
        return validadeMes;
    }

    public @NotBlank String getValidadeAno() {
        return validadeAno;
    }

    public @NotBlank String getNomeNoCartao() {
        return nomeNoCartao;
    }

    public @NotBlank String getCvv() {
        return cvv;
    }

    public @NotBlank String getTipoPagamento() {
        return tipoPagamento;
    }

    public @Positive Integer getParcelas() {
        return parcelas;
    }
}

