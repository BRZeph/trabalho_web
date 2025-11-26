package com.example.demo.controller;


import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/password")
@Tag(name = "Senha", description = "Redefinição de senha via e-mail")
public class PasswordController {

    private final PasswordResetService passwordResetService;

    public PasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(summary = "Solicita redefinição de senha (envia e-mail com link)")
    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@Valid @RequestBody ForgotPasswordRequest request) {

        // Sem retornar se o e-mail existe ou não, apenas retornamos OK
        passwordResetService.solicitarReset(request.getEmail());

        return ResponseEntity.ok(
                "Se o e-mail existir, enviaremos um link de redefinição."
        );
    }

    @Operation(summary = "Redefine senha usando o token enviado por e-mail")
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@Valid @RequestBody ResetPasswordRequest request) {
        boolean ok = passwordResetService.redefinirSenha(request.getToken(), request.getNovaSenha());

        if (!ok) {
            return ResponseEntity.badRequest().body("Token inválido ou expirado.");
        }

        return ResponseEntity.ok("Senha alterada com sucesso.");
    }
}
