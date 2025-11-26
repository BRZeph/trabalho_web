package com.example.demo.entity;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "assinaturas")
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plano_id")
    private Plano plano;

    @Column(nullable = false)
    private LocalDate ativoAte;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private String tipoPagamento; // "Crédito" / "Débito"

    @Column(nullable = false)
    private Integer parcelas;

    @Column(nullable = false)
    private String numeroCartaoMascarado; // ex: **** **** **** 1234

    public Assinatura() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public LocalDate getAtivoAte() {
        return ativoAte;
    }

    public void setAtivoAte(LocalDate ativoAte) {
        this.ativoAte = ativoAte;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }

    public String getNumeroCartaoMascarado() {
        return numeroCartaoMascarado;
    }

    public void setNumeroCartaoMascarado(String numeroCartaoMascarado) {
        this.numeroCartaoMascarado = numeroCartaoMascarado;
    }
}
