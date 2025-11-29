package com.example.demo.controller;

import com.example.demo.dto.PlanoResponse;
import com.example.demo.entity.Plano;
import com.example.demo.repository.PlanoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<List<PlanoResponse>> listar() {
        List<PlanoResponse> lista = planoRepository.findAll()
                .stream()
                .map(p -> new PlanoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getPrecoMensal(),
                        p.isAtivo(),
                        p.getDescricao()
                ))
                .toList();

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Busca um plano pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Optional<Plano> opt = planoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plano não encontrado.");
        }

        Plano p = opt.get();
        PlanoResponse resp = new PlanoResponse(
                p.getId(),
                p.getNome(),
                p.getPrecoMensal(),
                p.isAtivo(),
                p.getDescricao()
        );

        return ResponseEntity.ok(resp);
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

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "Atualiza um plano existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @RequestBody @Valid PlanoResponse request) {

        Optional<Plano> opt = planoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plano não encontrado.");
        }

        Plano plano = opt.get();
        plano.setNome(request.getNome());
        plano.setPrecoMensal(
                request.getPrecoMensal() != null ? request.getPrecoMensal() : BigDecimal.ZERO
        );
        plano.setAtivo(request.isAtivo());
        plano.setDescricao(request.getDescricao());

        Plano atualizado = planoRepository.save(plano);

        PlanoResponse resp = new PlanoResponse(
                atualizado.getId(),
                atualizado.getNome(),
                atualizado.getPrecoMensal(),
                atualizado.isAtivo(),
                atualizado.getDescricao()
        );

        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Remove (exclui) um plano pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        Optional<Plano> opt = planoRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Plano não encontrado.");
        }

        planoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
