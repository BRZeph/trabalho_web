package com.example.demo.repository;

import com.example.demo.entity.Assinatura;
import com.example.demo.entity.Plano;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanoRepository extends JpaRepository<Plano, Long> {
}
