package battleship;

import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorDeTempo {
    private final Stopwatch stopwatch;
    private final List<RegistoDeTempo> historico;
    private int contadorJogadas;

    public GestorDeTempo() {
        this.stopwatch = Stopwatch.createUnstarted();
        this.historico = new ArrayList<>();
        this.contadorJogadas = 0;
    }

    /**
     * Arranca ou reinicia o Stopwatch da Google para a jogada atual.
     */
    public void iniciarJogada() {
        contadorJogadas++;

        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
        stopwatch.reset();
        stopwatch.start();

        System.out.println("-> Tempo a contar para a jogada " + contadorJogadas + "...");
    }

    /**
     * Para o Stopwatch, regista a duração e mostra o tempo gasto na jogada.
     */
    public Duration finalizarJogada() {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }

        Duration duracao = stopwatch.elapsed();
        RegistoDeTempo registo = new RegistoDeTempo(contadorJogadas, duracao);
        historico.add(registo);

        System.out.println("-> Tempo gasto na jogada " + contadorJogadas + ": "
                + duracao.toSeconds() + "s (" + duracao.toMillis() + "ms)\n");

        return duracao;
    }

    public void mostrarHistorico() {
        System.out.println("\n=== HISTÓRICO DE TEMPOS DE JOGADA ===");
        if (historico.isEmpty()) {
            System.out.println("Nenhuma jogada foi efetuada ainda.");
        } else {
            for (RegistoDeTempo registo : historico) {
                System.out.println(registo);
            }
        }
        System.out.println("==================================\n");
    }

    /**
     * Reinicia o gestor de tempo para um novo jogo,
     * limpando o histórico e zerando o contador de jogadas.
     */
    public void reiniciar() {
        if (stopwatch.isRunning()) {
            stopwatch.stop();
        }
        stopwatch.reset();
        this.contadorJogadas = 0;
        this.historico.clear();
    }

    public List<RegistoDeTempo> getHistorico() {
        return Collections.unmodifiableList(historico);
    }
}
