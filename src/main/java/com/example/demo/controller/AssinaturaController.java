package com.example.demo.controller;

import com.example.demo.dto.AssinaturaResponse;
import com.example.demo.dto.CriarAssinaturaRequest;
import com.example.demo.entity.Assinatura;
import com.example.demo.entity.Plano;
import com.example.demo.entity.User;
import com.example.demo.repository.AssinaturaRepository;
import com.example.demo.repository.PlanoRepository;
import com.example.demo.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping
@Tag(name = "Assinaturas", description = "Assinatura de planos")
public class AssinaturaController {

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;
    private final UserRepository usuarioRepository;

    public AssinaturaController(AssinaturaRepository assinaturaRepository,
                                PlanoRepository planoRepository,
                                UserRepository usuarioRepository) {
        this.assinaturaRepository = assinaturaRepository;
        this.planoRepository = planoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Cria uma assinatura (Renovar plano) para o usuário logado")
    @PostMapping("/assinaturas")
    public ResponseEntity<?> criarAssinatura(@Valid @RequestBody CriarAssinaturaRequest request,
                                             Authentication authentication) {
        System.out.println("Endpoint: criar assinatura");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Se já tiver plano ativo, não deixa cadastrar outro
        Optional<Assinatura> existente = assinaturaRepository.findByUsuarioAndAtivoTrue(usuario);
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Usuário já possui um plano ativo.");
        }

        Plano plano = planoRepository.findById(request.getPlanoId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        Assinatura nova = new Assinatura();
        nova.setUsuario(usuario);
        nova.setPlano(plano);
        nova.setTipoPagamento(request.getTipoPagamento());
        nova.setParcelas(request.getParcelas());
        nova.setNumeroCartaoMascarado(
                "**** **** **** " + request.getNumeroCartao().substring(request.getNumeroCartao().length() - 4)
        );

        // Fixo: plano mensal -> +30 dias
        nova.setAtivoAte(LocalDate.now().plusDays(30));
        nova.setAtivo(true);

        assinaturaRepository.save(nova);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AssinaturaResponse(plano.getNome(), nova.getAtivoAte(), nova.isAtivo()));
    }

    @Operation(summary = "Retorna o plano ativo do usuário logado")
    @GetMapping("/me/plano")
    public ResponseEntity<?> planoAtivo(Authentication authentication) {
        System.out.println("Endpoint: plano ativo");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Assinatura> assinatura = assinaturaRepository.findByUsuarioAndAtivoTrue(usuario);
        if (assinatura.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        Assinatura a = assinatura.get();
        AssinaturaResponse resp = new AssinaturaResponse(
                a.getPlano().getNome(),
                a.getAtivoAte(),
                a.isAtivo()
        );

        return ResponseEntity.ok(resp);
    }
}
