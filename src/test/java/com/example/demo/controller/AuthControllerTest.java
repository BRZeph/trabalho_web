package com.example.demo.controller;


import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginDeveRetornarTokenQuandoCredenciaisValidas() {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setSenha("123456");

        Authentication authentication = mock(Authentication.class);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(request.getEmail())
                .password("encoded")
                .authorities("ROLE_USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("fake-jwt");

        // act
        ResponseEntity<?> response = authController.login(request);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals("fake-jwt", body.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void loginDeveRetornar401QuandoCredenciaisInvalidas() {
        // arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setSenha("senhaErrada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // act
        ResponseEntity<?> response = authController.login(request);

        // assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Credenciais inválidas", response.getBody());
    }

    @Test
    void registerDeveRetornar409QuandoEmailJaExistir() {
        // arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("jaexiste@example.com");
        request.setSenha("123456");

        User existing = new User();
        existing.setEmail(request.getEmail());

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existing));

        // act
        ResponseEntity<?> response = authController.register(request);

        // assert
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("E-mail já está em uso.", response.getBody());

        verify(usuarioRepository, never()).save(any(User.class));
    }

    @Test
    void registerDeveCriarUsuarioERetornarToken() {
        // arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("novo@example.com");
        request.setSenha("123456");

        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getSenha()))
                .thenReturn("encoded-pass");

        User salvo = new User();
        salvo.setEmail(request.getEmail());
        salvo.setPassword("encoded-pass");

        when(usuarioRepository.save(any(User.class)))
                .thenReturn(salvo);

        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("jwt-123");

        // act
        ResponseEntity<?> response = authController.register(request);

        // assert
        assertEquals(201, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof LoginResponse);

        LoginResponse body = (LoginResponse) response.getBody();
        assertEquals("jwt-123", body.getToken());

        // captura o User passado para o save pra garantir que foi montado certo
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usuarioRepository).save(userCaptor.capture());
        User userSalvo = userCaptor.getValue();
        assertEquals("novo@example.com", userSalvo.getEmail());
        assertEquals("encoded-pass", userSalvo.getPassword());

        verify(passwordEncoder).encode("123456");
    }

    @Test
    void meDeveRetornarNomeDoUsuarioAutenticado() {
        // arrange
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@example.com");

        // act
        ResponseEntity<String> response = authController.me(authentication);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Usuário autenticado: user@example.com", response.getBody());
    }
}