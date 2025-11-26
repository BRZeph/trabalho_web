package com.example.demo.controller;


import com.example.demo.dto.PlanoResponse;
import com.example.demo.entity.Plano;
import com.example.demo.repository.PlanoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/planos")
@Tag(name = "Planos", description = "Cadastro e consulta de planos")
public class PlanoController {

    private final PlanoRepository planoRepository;

    public PlanoController(PlanoRepository planoRepository) {
        this.planoRepository = planoRepository;
    }

    @Operation(summary = "Lista todos os planos")
    @GetMapping
    public List<PlanoResponse> listar() {
        return planoRepository.findAll()
                .stream()
                .map(p -> new PlanoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getPrecoMensal(),
                        p.isAtivo(),
                        p.getDescricao()
                ))
                .toList();
    }

    @Operation(summary = "Cria um novo plano")
    @PostMapping
    public ResponseEntity<PlanoResponse> criar(@RequestBody @Valid PlanoResponse request) {
        Plano plano = new Plano();
        plano.setNome(request.getNome());
        plano.setPrecoMensal(
                request.getPrecoMensal() != null ? request.getPrecoMensal() : BigDecimal.ZERO
        );
        plano.setAtivo(request.isAtivo());
        plano.setDescricao(request.getDescricao());

        Plano salvo = planoRepository.save(plano);

        PlanoResponse resp = new PlanoResponse(
                salvo.getId(),
                salvo.getNome(),
                salvo.getPrecoMensal(),
                salvo.isAtivo(),
                salvo.getDescricao()
        );

        return ResponseEntity.ok(resp);
    }
}
