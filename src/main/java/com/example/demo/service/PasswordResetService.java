package com.example.demo.service;


import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private final String deepLinkBase = "http://localhost:51028/#/reset_password";

    public PasswordResetService(UserRepository usuarioRepository,
                                PasswordResetTokenRepository tokenRepository,
                                JavaMailSender mailSender,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    public void solicitarReset(String email) {
        Optional<User> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return;
        }

        User usuario = usuarioOpt.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracao = LocalDateTime.now().plusHours(1);

        PasswordResetToken prt = new PasswordResetToken(token, usuario, expiracao);
        tokenRepository.save(prt);

        String link = deepLinkBase + "?token=" + token;
//        String link = "http://localhost:51028/#/reset_password?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(usuario.getEmail());
        msg.setSubject("Redefinição de senha");
        msg.setText(
                "Olá,\n\n" +
                        "Você solicitou a redefinição de senha do aplicativo.\n" +
                        "Para definir uma nova senha, clique no link abaixo (no celular onde o app está instalado):\n\n" +
                        link + "\n\n" +
                        "Se você não solicitou, ignore este e-mail.\n"
        );

        mailSender.send(msg);
    }

    public boolean redefinirSenha(String token, String novaSenha) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken prt = tokenOpt.get();

        if (prt.isUsado() || prt.getExpiracao().isBefore(LocalDateTime.now())) {
            return false;
        }

        User usuario = prt.getUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        prt.setUsado(true);
        tokenRepository.save(prt);

        return true;
    }
}
