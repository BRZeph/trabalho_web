package com.example.demo.controller;

import com.example.demo.dto.PlanoResponse;
import com.example.demo.entity.Plano;
import com.example.demo.repository.PlanoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanoControllerTest {

    @Mock
    private PlanoRepository planoRepository;

    @InjectMocks
    private PlanoController planoController;

    @Test
    void listarDeveRetornarListaDePlanosMapeadosParaResponse() {
        // arrange
        Plano plano = new Plano();
        // se sua entidade tiver setId, pode usar; senão, ignore o id nos asserts
        // plano.setId(1L);
        plano.setNome("Plano Básico");
        plano.setPrecoMensal(new BigDecimal("19.90"));
        plano.setAtivo(true);
        plano.setDescricao("Plano básico de teste");

        when(planoRepository.findAll()).thenReturn(List.of(plano));

        // act
        List<PlanoResponse> resposta = planoController.listar();

        // assert
        assertEquals(1, resposta.size());
        PlanoResponse pr = resposta.get(0);

        assertEquals("Plano Básico", pr.getNome());
        assertEquals(new BigDecimal("19.90"), pr.getPrecoMensal());
        assertTrue(pr.isAtivo());
        assertEquals("Plano básico de teste", pr.getDescricao());
    }

    @Test
    void criarDeveSalvarEDevolverPlanoResponseComPrecoZeroQuandoNulo() {
        // arrange: request vindo da API (usa o mesmo DTO para request/response)
        PlanoResponse request = new PlanoResponse(
                null,
                "Plano Premium",
                null,             // precoMensal nulo -> deve virar BigDecimal.ZERO no entity
                true,
                "Plano premium de teste"
        );

        Plano salvo = new Plano();
        // salvo.setId(10L); // use se tiver setter para id
        salvo.setNome("Plano Premium");
        salvo.setPrecoMensal(BigDecimal.ZERO);
        salvo.setAtivo(true);
        salvo.setDescricao("Plano premium de teste");

        when(planoRepository.save(any(Plano.class))).thenReturn(salvo);

        // act
        ResponseEntity<PlanoResponse> response = planoController.criar(request);

        // assert
        assertEquals(200, response.getStatusCodeValue());
        PlanoResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Plano Premium", body.getNome());
        assertEquals(BigDecimal.ZERO, body.getPrecoMensal());
        assertTrue(body.isAtivo());
        assertEquals("Plano premium de teste", body.getDescricao());

        // garante que o objeto salvo foi montado corretamente a partir do request
        ArgumentCaptor<Plano> planoCaptor = ArgumentCaptor.forClass(Plano.class);
        verify(planoRepository).save(planoCaptor.capture());
        Plano planoSalvo = planoCaptor.getValue();

        assertEquals("Plano Premium", planoSalvo.getNome());
        assertEquals(BigDecimal.ZERO, planoSalvo.getPrecoMensal()); // nulo -> ZERO
        assertTrue(planoSalvo.isAtivo());
        assertEquals("Plano premium de teste", planoSalvo.getDescricao());
    }
}
