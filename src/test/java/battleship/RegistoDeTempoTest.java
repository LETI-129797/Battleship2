package battleship;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RegistoDeTempoTest {

    @Test
    void testConstrutorEGetters() {
        Duration duracao = Duration.ofMillis(1500); // 1.5 segundos
        RegistoDeTempo registo = new RegistoDeTempo(3, duracao);

        assertEquals(3, registo.getContadorJogadas());
        assertEquals(duracao, registo.getDuracao());
    }

    @Test
    void testToString() {
        Duration duracao = Duration.ofMillis(2500); // 2s (2500ms)
        RegistoDeTempo registo = new RegistoDeTempo(1, duracao);

        String expected = "Jogada 1: 2s (2500ms)";
        assertEquals(expected, registo.toString());
    }
}