package br.com.fiap.petfiap.model;

import br.com.fiap.petfiap.exception.StatusInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

// Testes unitarios das transicoes de status da classe abstrata Atendimento
// (exercitadas por meio de um Banho): sem banco, sem Spring (Aula 15).
public class AtendimentoTest {

    private Banho banhoAgendado() {
        return new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    public void deveRecusarCancelamentoQuandoAtendimentoJaConcluido() {
        // Arrange
        Banho atendimento = banhoAgendado();
        atendimento.concluir();

        // Act + Assert: atendimento ja realizado nao pode ser cancelado
        assertThrows(StatusInvalidoException.class, atendimento::cancelar);
    }

    @Test
    public void deveRecusarCancelamentoQuandoAtendimentoJaCancelado() {
        // Arrange
        Banho atendimento = banhoAgendado();
        atendimento.cancelar();

        // Act + Assert: cancelar duas vezes nao e permitido
        assertThrows(StatusInvalidoException.class, atendimento::cancelar);
    }
}
