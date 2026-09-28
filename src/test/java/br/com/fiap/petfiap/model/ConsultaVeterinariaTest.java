package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Testes unitarios do model: sem banco, sem Spring (Aula 15).
public class ConsultaVeterinariaTest {

    private ConsultaVeterinaria consultaDaMimi() {
        return new ConsultaVeterinaria(1, "Mimi", "MEDIO", "Bruno", LocalDateTime.of(2026, 10, 1, 14, 0));
    }

    @Test
    public void deveAcumular50PontosDeFidelidade() {
        // Act
        int pontos = consultaDaMimi().calcularPontosFidelidade();

        // Assert
        assertEquals(50, pontos);
    }

    @Test
    public void deveDurar30Minutos() {
        // Act
        int duracao = consultaDaMimi().getDuracaoMinutos();

        // Assert
        assertEquals(30, duracao);
    }

    @Test
    public void deveCustar150ReaisIndependenteDoPorte() {
        // Arrange: mesma consulta para portes diferentes
        ConsultaVeterinaria pequeno = new ConsultaVeterinaria(2, "Bidu", "PEQUENO", "Carla", LocalDateTime.of(2026, 10, 1, 15, 0));
        ConsultaVeterinaria grande = new ConsultaVeterinaria(3, "Thor", "GRANDE", "Diego", LocalDateTime.of(2026, 10, 1, 16, 0));

        // Act + Assert: o porte nao muda o preco da consulta
        assertEquals(150.0, pequeno.calcularPreco(), 0.001);
        assertEquals(150.0, consultaDaMimi().calcularPreco(), 0.001);
        assertEquals(150.0, grande.calcularPreco(), 0.001);
    }
}
