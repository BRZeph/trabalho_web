package com.example.demo.dto;

import java.time.LocalDate;

public class AssinaturaResponse {

    private String planoNome;
    private LocalDate ativoAte;
    private boolean ativo;

    public AssinaturaResponse(String planoNome, LocalDate ativoAte, boolean ativo) {
        this.planoNome = planoNome;
        this.ativoAte = ativoAte;
        this.ativo = ativo;
    }
}
