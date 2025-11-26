package com.example.demo.dto;


import java.math.BigDecimal;

public class PlanoResponse {

    private Long id;
    private String nome;
    private BigDecimal precoMensal;
    private boolean ativo;
    private String descricao;

    public PlanoResponse(Long id, String nome, BigDecimal precoMensal, boolean ativo, String descricao) {
        this.id = id;
        this.nome = nome;
        this.precoMensal = precoMensal;
        this.ativo = ativo;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoMensal() {
        return precoMensal;
    }

    public void setPrecoMensal(BigDecimal precoMensal) {
        this.precoMensal = precoMensal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
