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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                "**** **** **** " + request.getNumeroCartao()
                        .substring(request.getNumeroCartao().length() - 4)
        );

        // Exemplo: plano mensal -> +30 dias a partir de hoje
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

    // ---------- CRUD COMPLEMENTAR ----------

    @Operation(summary = "Lista todas as assinaturas do usuário logado (ativas e inativas)")
    @GetMapping("/assinaturas")
    public ResponseEntity<List<AssinaturaResponse>> listarAssinaturas(Authentication authentication) {
        System.out.println("Endpoint: listar assinaturas");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Busca todas as assinaturas e filtra pelo usuário logado
        List<AssinaturaResponse> lista = assinaturaRepository.findAll()
                .stream()
                .filter(a -> a.getUsuario() != null &&
                        a.getUsuario().getId().equals(usuario.getId()))
                .map(a -> new AssinaturaResponse(
                        a.getPlano().getNome(),
                        a.getAtivoAte(),
                        a.isAtivo()
                ))
                .collect(Collectors.toList());

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Busca uma assinatura específica do usuário logado por ID")
    @GetMapping("/assinaturas/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id,
                                         Authentication authentication) {
        System.out.println("Endpoint: buscar assinatura por id");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Assinatura> opt = assinaturaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Assinatura não encontrada.");
        }

        Assinatura a = opt.get();

        // Garante que a assinatura pertence ao usuário logado
        if (a.getUsuario() == null || !a.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Assinatura não pertence ao usuário logado.");
        }

        AssinaturaResponse resp = new AssinaturaResponse(
                a.getPlano().getNome(),
                a.getAtivoAte(),
                a.isAtivo()
        );
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Atualiza uma assinatura do usuário logado")
    @PutMapping("/assinaturas/{id}")
    public ResponseEntity<?> atualizarAssinatura(@PathVariable Long id,
                                                 @Valid @RequestBody CriarAssinaturaRequest request,
                                                 Authentication authentication) {
        System.out.println("Endpoint: atualizar assinatura");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Assinatura> opt = assinaturaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Assinatura não encontrada.");
        }

        Assinatura a = opt.get();

        // Garante que a assinatura pertence ao usuário logado
        if (a.getUsuario() == null || !a.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Assinatura não pertence ao usuário logado.");
        }

        Plano plano = planoRepository.findById(request.getPlanoId())
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        a.setPlano(plano);
        a.setTipoPagamento(request.getTipoPagamento());
        a.setParcelas(request.getParcelas());
        a.setNumeroCartaoMascarado(
                "**** **** **** " + request.getNumeroCartao()
                        .substring(request.getNumeroCartao().length() - 4)
        );

        // Exemplo: ao atualizar, renova por mais 30 dias
        a.setAtivoAte(LocalDate.now().plusDays(30));
        a.setAtivo(true);

        assinaturaRepository.save(a);

        AssinaturaResponse resp = new AssinaturaResponse(
                a.getPlano().getNome(),
                a.getAtivoAte(),
                a.isAtivo()
        );

        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Cancela uma assinatura do usuário logado (desativa o plano)")
    @DeleteMapping("/assinaturas/{id}")
    public ResponseEntity<?> cancelarAssinatura(@PathVariable Long id,
                                                Authentication authentication) {
        System.out.println("Endpoint: cancelar assinatura");
        String email = authentication.getName();
        User usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Optional<Assinatura> opt = assinaturaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Assinatura não encontrada.");
        }

        Assinatura a = opt.get();

        // Garante que a assinatura pertence ao usuário logado
        if (a.getUsuario() == null || !a.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Assinatura não pertence ao usuário logado.");
        }

        // "Cancelar" = marcar como inativa
        a.setAtivo(false);
        assinaturaRepository.save(a);

        return ResponseEntity.noContent().build();
    }
}
