package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Test class for class GestorDeTempo.
 *
 * Author: ${user.name}
 * Date: 2026-11-17 20:01
 *
 * Cyclomatic Complexity:
 * GestorDeTempo(): 1
 * iniciarJogada(): 1
 * finalizarJogada(): 2
 * mostrarHistorico(): 2
 * reiniciar(): 1
 * reiniciarStopwatch(): 2
 * getHistorico(): 1
 */
class GestorDeTempoTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        // Redireciona o System.out para podermos validar os printlns
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        // Restaura o System.out original após cada teste
        System.setOut(originalOut);
    }

    @Test
    void testIniciarEFinalizarJogada() throws InterruptedException {
        GestorDeTempo gestor = new GestorDeTempo();

        gestor.iniciarJogada();
        // Esperamos um pouco para garantir que a duração é maior que zero
        Thread.sleep(15);
        Duration duracao = gestor.finalizarJogada();

        assertNotNull(duracao);
        assertTrue(duracao.toMillis() > 0);

        List<RegistoDeTempo> historico = gestor.getHistorico();
        assertEquals(1, historico.size());
        assertEquals(1, historico.get(0).getContadorJogadas());

        // Verifica as impressões na consola
        String output = outContent.toString();
        assertTrue(output.contains("Tempo a contar para a jogada 1"));
        assertTrue(output.contains("Tempo gasto na jogada 1"));
    }

    @Test
    void testFinalizarJogadaSemIniciar() {
        GestorDeTempo gestor = new GestorDeTempo();

        // Testa o "if (stopwatch.isRunning())" no cenário falso
        Duration duracao = gestor.finalizarJogada();

        assertNotNull(duracao);
        assertEquals(0, duracao.toMillis()); // Cronómetro não andou
        assertEquals(1, gestor.getHistorico().size()); // Mas registou a jogada 0
    }

    @Test
    void testMostrarHistoricoVazio() {
        GestorDeTempo gestor = new GestorDeTempo();
        gestor.mostrarHistorico();

        // Testa a ramificação verdadeira: "if (historico.isEmpty())"
        assertTrue(outContent.toString().contains("Nenhuma jogada foi efetuada ainda."));
    }

    @Test
    void testMostrarHistoricoComRegistos() {
        GestorDeTempo gestor = new GestorDeTempo();
        gestor.iniciarJogada();
        gestor.finalizarJogada();

        outContent.reset(); // Limpa o output anterior para não confundir
        gestor.mostrarHistorico();

        // Testa a ramificação falsa (imprimir histórico com dados)
        String output = outContent.toString();
        assertTrue(output.contains("=== HISTÓRICO DE TEMPOS DE JOGADA ==="));
        assertTrue(output.contains("Jogada 1:"));
    }

    @Test
    void testReiniciarComStopwatchEmExecucao() {
        GestorDeTempo gestor = new GestorDeTempo();

        // Inicia a jogada, deixando o stopwatch a correr
        gestor.iniciarJogada();
        gestor.finalizarJogada();

        // Força uma nova jogada para garantir que o stopwatch está a executar
        // no momento em que chamamos reiniciar()
        gestor.iniciarJogada();

        gestor.reiniciar();

        // Testa se limpou tudo com sucesso
        assertTrue(gestor.getHistorico().isEmpty());
    }

    @Test
    void testGetHistoricoImutavel() {
        GestorDeTempo gestor = new GestorDeTempo();
        List<RegistoDeTempo> historico = gestor.getHistorico();

        // Garante que é uma coleção "unmodifiable", lançando erro se tentarem alterar fora da classe
        assertThrows(UnsupportedOperationException.class, () -> {
            historico.add(new RegistoDeTempo(1, Duration.ofMillis(10)));
        });
    }
}